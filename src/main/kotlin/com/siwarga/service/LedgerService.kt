package com.siwarga.service

import com.siwarga.domain.entity.DuesLedger
import com.siwarga.domain.entity.House
import com.siwarga.repository.DuesLedgerRepository
import com.siwarga.repository.HouseRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth

@Service
@Transactional
class LedgerService(
    private val ledgerRepository: DuesLedgerRepository,
    private val houseRepository: HouseRepository
) {
    companion object {
        val SYSTEM_START: YearMonth = YearMonth.of(2024, 1)
    }

    private val log = LoggerFactory.getLogger(LedgerService::class.java)

    /**
     * Ensures all ledger rows from SYSTEM_START up to [upToMonth] exist for the given house.
     * Idempotent — rows that already exist are not modified.
     */
    fun ensureLedgerExists(house: House, upToMonth: YearMonth) {
        var current = SYSTEM_START
        while (!current.isAfter(upToMonth)) {
            val year = current.year
            val month = current.monthValue
            if (ledgerRepository.findByHouseIdAndPeriodYearAndPeriodMonth(house.id, year, month).isEmpty) {
                ledgerRepository.save(
                    DuesLedger(
                        house = house,
                        periodYear = year,
                        periodMonth = month,
                        dueDate = LocalDate.of(year, month, 1)
                    )
                )
            }
            current = current.plusMonths(1)
        }
    }

    /**
     * Ensures a specific future ledger row exists (used when allocating advance payments).
     * Returns the ledger row (created or existing).
     */
    fun ensureAndGetLedger(house: House, yearMonth: YearMonth): DuesLedger {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        return ledgerRepository.findByHouseIdAndPeriodYearAndPeriodMonth(house.id, year, month)
            .orElseGet {
                ledgerRepository.save(
                    DuesLedger(
                        house = house,
                        periodYear = year,
                        periodMonth = month,
                        dueDate = LocalDate.of(year, month, 1)
                    )
                )
            }
    }

    /**
     * Scheduled job: runs on the 1st of every month at 00:05 to create ledger rows
     * for the new month for all existing houses.
     */
    @Scheduled(cron = "0 5 0 1 * *")
    fun createMonthlyLedgerRows() {
        val currentMonth = YearMonth.now()
        val houses = houseRepository.findAll()
        log.info("Creating ledger rows for {} for {} houses", currentMonth, houses.size)
        houses.forEach { house ->
            ensureAndGetLedger(house, currentMonth)
        }
    }
}
