package com.siwarga.service

import com.siwarga.domain.enums.LedgerStatus
import com.siwarga.dto.response.ArrearDetail
import com.siwarga.dto.response.ArrearsStatusResponse
import com.siwarga.dto.response.FineDetail
import com.siwarga.dto.response.HouseFinancialStandingResponse
import com.siwarga.dto.response.MonthlyReportResponse
import com.siwarga.dto.response.MonthlyReportRow
import com.siwarga.exception.ResourceNotFoundException
import com.siwarga.repository.DuesLedgerRepository
import com.siwarga.repository.HouseRepository
import com.siwarga.repository.ResidentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth

@Service
class ReportService(
    private val houseRepository: HouseRepository,
    private val residentRepository: ResidentRepository,
    private val ledgerRepository: DuesLedgerRepository,
    private val ledgerService: LedgerService,
    private val fineService: FineService
) {

    @Transactional
    fun getArrearsStatus(houseId: Long): ArrearsStatusResponse {
        val house = houseRepository.findById(houseId)
            .orElseThrow { ResourceNotFoundException("House with id $houseId not found") }
        val resident = residentRepository.findByHouseId(houseId).orElse(null)
        val currentMonth = YearMonth.now()

        // Ensure ledger and fines are up to date (writable ops inside a read tx is fine for this case,
        // but we use a non-read-only method to allow flushing)
        ledgerService.ensureLedgerExists(house, currentMonth)
        fineService.computeAndPersistFines(house)

        val unpaidLedgers = ledgerRepository.findUnpaidUpToMonth(
            houseId, currentMonth.year, currentMonth.monthValue,
            listOf(LedgerStatus.UNPAID, LedgerStatus.PARTIAL)
        )
        val unpaidFines = fineService.getUnpaidFines(houseId)

        val totalDuesOwed = unpaidLedgers.sumOf { it.amountDue - it.amountPaid }
        val totalFinesOwed = unpaidFines.sumOf { it.amount - it.amountPaid }

        return ArrearsStatusResponse(
            houseId = house.id,
            houseDisplay = house.displayName,
            rtName = house.rt.name,
            residentName = resident?.name,
            unpaidMonths = unpaidLedgers.map { "%d-%02d".format(it.periodYear, it.periodMonth) },
            totalDuesOwed = totalDuesOwed,
            totalFinesOwed = totalFinesOwed,
            totalOwed = totalDuesOwed + totalFinesOwed,
            fineBreakdown = unpaidFines.map { fine ->
                FineDetail(
                    id = fine.id,
                    fineType = fine.fineType.name,
                    amount = fine.amount,
                    amountPaid = fine.amountPaid,
                    amountOwed = fine.amount - fine.amountPaid,
                    triggerMonth = fine.triggerMonth,
                    status = fine.status.name
                )
            }
        )
    }

    @Transactional
    fun getFinancialStanding(houseId: Long): HouseFinancialStandingResponse {
        val house = houseRepository.findById(houseId)
            .orElseThrow { ResourceNotFoundException("House with id $houseId not found") }
        val resident = residentRepository.findByHouseId(houseId).orElse(null)
        val currentMonth = YearMonth.now()

        ledgerService.ensureLedgerExists(house, currentMonth)
        fineService.computeAndPersistFines(house)

        val unpaidLedgers = ledgerRepository.findUnpaidUpToMonth(
            houseId, currentMonth.year, currentMonth.monthValue,
            listOf(LedgerStatus.UNPAID, LedgerStatus.PARTIAL)
        )
        val unpaidFines = fineService.getUnpaidFines(houseId)
        val futurePaidLedgers = ledgerRepository.findFuturePaidLedgers(
            houseId, currentMonth.year, currentMonth.monthValue
        )

        val totalArrearsOwed = unpaidLedgers.sumOf { it.amountDue - it.amountPaid }
        val totalFinesOwed = unpaidFines.sumOf { it.amount - it.amountPaid }
        val prepaidBalance = futurePaidLedgers.sumOf { it.amountPaid }
        val netBalance = totalArrearsOwed + totalFinesOwed - prepaidBalance

        return HouseFinancialStandingResponse(
            houseId = house.id,
            houseDisplay = house.displayName,
            rtName = house.rt.name,
            residentName = resident?.name,
            prepaidBalance = prepaidBalance,
            arrears = unpaidLedgers.map { ledger ->
                ArrearDetail(
                    period = "%d-%02d".format(ledger.periodYear, ledger.periodMonth),
                    amountDue = ledger.amountDue,
                    amountPaid = ledger.amountPaid,
                    amountOwed = ledger.amountDue - ledger.amountPaid,
                    status = ledger.status.name,
                )
            },
            totalArrearsOwed = totalArrearsOwed,
            fines = unpaidFines.map { fine ->
                FineDetail(
                    id = fine.id,
                    fineType = fine.fineType.name,
                    amount = fine.amount,
                    amountPaid = fine.amountPaid,
                    amountOwed = fine.amount - fine.amountPaid,
                    triggerMonth = fine.triggerMonth,
                    status = fine.status.name,
                )
            },
            totalFinesOwed = totalFinesOwed,
            netBalance = netBalance,
        )
    }

    @Transactional(readOnly = true)
    fun getMonthlyReport(year: Int, month: Int, rtId: Long? = null): MonthlyReportResponse {
        val houses = if (rtId != null) houseRepository.findByRtId(rtId) else houseRepository.findAll()

        val rows = houses.map { house ->
            val ledger = ledgerRepository.findByHouseIdAndPeriodYearAndPeriodMonth(house.id, year, month).orElse(null)
            val resident = residentRepository.findByHouseId(house.id).orElse(null)
            MonthlyReportRow(
                houseId = house.id,
                rtName = house.rt.name,
                block = house.block,
                houseNumber = house.houseNumber,
                residentName = resident?.name,
                status = ledger?.status?.name ?: "NO_RECORD",
                amountDue = ledger?.amountDue ?: 120_000L,
                amountPaid = ledger?.amountPaid ?: 0L,
                paidDate = ledger?.paidDate?.toString()
            )
        }

        return MonthlyReportResponse(
            year = year,
            month = month,
            totalHouses = rows.size,
            totalPaid = rows.count { it.status == LedgerStatus.PAID.name },
            totalPartial = rows.count { it.status == LedgerStatus.PARTIAL.name },
            totalUnpaid = rows.count { it.status == LedgerStatus.UNPAID.name || it.status == "NO_RECORD" },
            totalCollected = rows.sumOf { it.amountPaid },
            rows = rows
        )
    }
}
