package com.siwarga.service

import com.siwarga.domain.entity.DuesLedger
import com.siwarga.domain.entity.Fine
import com.siwarga.domain.entity.House
import com.siwarga.domain.entity.Rt
import com.siwarga.domain.entity.Rw
import com.siwarga.domain.enums.FineType
import com.siwarga.domain.enums.LedgerStatus
import com.siwarga.repository.DuesLedgerRepository
import com.siwarga.repository.FineRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class FineServiceTest {

    @Mock lateinit var fineRepository: FineRepository
    @Mock lateinit var ledgerRepository: DuesLedgerRepository
    @InjectMocks lateinit var fineService: FineService

    @Captor lateinit var fineCaptor: ArgumentCaptor<Fine>

    private val rw = Rw(id = 1L, name = "RW 01")
    private val rt = Rt(id = 1L, rw = rw, name = "RT 01")
    private val house = House(id = 1L, rt = rt, block = "E", houseNumber = "20")

    private fun ledger(year: Int, month: Int, status: LedgerStatus) = DuesLedger(
        id = (year * 12 + month).toLong(),
        house = house,
        periodYear = year,
        periodMonth = month,
        dueDate = LocalDate.of(year, month, 1),
        status = status
    )

    @Test
    fun `no fines when all months paid`() {
        val ledgers = (1..12).map { ledger(2024, it, LedgerStatus.PAID) }
        `when`(ledgerRepository.findByHouseIdOrderByPeriodYearAscPeriodMonthAsc(house.id)).thenReturn(ledgers)

        fineService.computeAndPersistFines(house)

        verify(fineRepository, never()).save(any())
    }

    @Test
    fun `six month fine fires at 6 consecutive unpaid`() {
        val ledgers = (1..6).map { ledger(2024, it, LedgerStatus.UNPAID) }
        `when`(ledgerRepository.findByHouseIdOrderByPeriodYearAscPeriodMonthAsc(house.id)).thenReturn(ledgers)
        `when`(fineRepository.existsByHouseIdAndFineTypeAndTriggerMonth(house.id, FineType.SIX_MONTH, 6)).thenReturn(false)

        fineService.computeAndPersistFines(house)

        verify(fineRepository).save(fineCaptor.capture())
        val fine = fineCaptor.value
        assertEquals(FineType.SIX_MONTH, fine.fineType)
        assertEquals(60_000L, fine.amount)
        assertEquals(6, fine.triggerMonth)
    }

    @Test
    fun `both six and twelve month fines fire at 12 consecutive unpaid`() {
        val ledgers = (1..12).map { ledger(2024, it, LedgerStatus.UNPAID) }
        `when`(ledgerRepository.findByHouseIdOrderByPeriodYearAscPeriodMonthAsc(house.id)).thenReturn(ledgers)
        `when`(fineRepository.existsByHouseIdAndFineTypeAndTriggerMonth(any(), any(), any())).thenReturn(false)

        fineService.computeAndPersistFines(house)

        // 3 fines: SIX_MONTH at position 6, SIX_MONTH at position 12, TWELVE_MONTH at position 12
        verify(fineRepository, times(3)).save(any())
    }

    @Test
    fun `fine counter resets after a payment`() {
        // 5 unpaid, 1 paid, then 6 more unpaid -> fine triggers at position 6 of second run
        val ledgers = (1..5).map { ledger(2024, it, LedgerStatus.UNPAID) } +
                listOf(ledger(2024, 6, LedgerStatus.PAID)) +
                (7..12).map { ledger(2024, it, LedgerStatus.UNPAID) }
        `when`(ledgerRepository.findByHouseIdOrderByPeriodYearAscPeriodMonthAsc(house.id)).thenReturn(ledgers)
        `when`(fineRepository.existsByHouseIdAndFineTypeAndTriggerMonth(house.id, FineType.SIX_MONTH, 6)).thenReturn(false)

        fineService.computeAndPersistFines(house)

        verify(fineRepository, times(1)).save(fineCaptor.capture())
        assertEquals(6, fineCaptor.value.triggerMonth)  // new run position 6
    }

    @Test
    fun `does not duplicate existing fines`() {
        val ledgers = (1..6).map { ledger(2024, it, LedgerStatus.UNPAID) }
        `when`(ledgerRepository.findByHouseIdOrderByPeriodYearAscPeriodMonthAsc(house.id)).thenReturn(ledgers)
        `when`(fineRepository.existsByHouseIdAndFineTypeAndTriggerMonth(house.id, FineType.SIX_MONTH, 6)).thenReturn(true)

        fineService.computeAndPersistFines(house)

        verify(fineRepository, never()).save(any())
    }
}
