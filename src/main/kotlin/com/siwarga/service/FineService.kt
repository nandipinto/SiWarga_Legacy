package com.siwarga.service

import com.siwarga.domain.entity.Fine
import com.siwarga.domain.entity.House
import com.siwarga.domain.enums.FineType
import com.siwarga.domain.enums.LedgerStatus
import com.siwarga.repository.DuesLedgerRepository
import com.siwarga.repository.FineRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FineService(
    private val fineRepository: FineRepository,
    private val ledgerRepository: DuesLedgerRepository
) {

    /**
     * Walks all ledger rows for the house chronologically, counts consecutive unpaid months,
     * and creates fine records at the appropriate thresholds if they don't already exist.
     *
     * Fine rules:
     * - Every 6th consecutive unpaid month: SIX_MONTH fine of Rp 60,000
     * - Every 12th consecutive unpaid month: TWELVE_MONTH fine of Rp 120,000
     *   (both fire at months 12, 24, 36, ...)
     */
    fun computeAndPersistFines(house: House) {
        val ledgers = ledgerRepository.findByHouseIdOrderByPeriodYearAscPeriodMonthAsc(house.id)
        var consecutiveUnpaid = 0

        for (ledger in ledgers) {
            if (ledger.status == LedgerStatus.PAID) {
                consecutiveUnpaid = 0
            } else {
                consecutiveUnpaid++
                val position = consecutiveUnpaid

                if (position % 6 == 0) {
                    if (!fineRepository.existsByHouseIdAndFineTypeAndTriggerMonth(house.id, FineType.SIX_MONTH, position)) {
                        fineRepository.save(Fine(
                            house = house,
                            fineType = FineType.SIX_MONTH,
                            amount = 60_000L,
                            triggerMonth = position
                        ))
                    }
                }

                if (position % 12 == 0) {
                    if (!fineRepository.existsByHouseIdAndFineTypeAndTriggerMonth(house.id, FineType.TWELVE_MONTH, position)) {
                        fineRepository.save(Fine(
                            house = house,
                            fineType = FineType.TWELVE_MONTH,
                            amount = 120_000L,
                            triggerMonth = position
                        ))
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true)
    fun getUnpaidFines(houseId: Long): List<Fine> =
        fineRepository.findByHouseIdAndStatusInOrderByTriggerMonthAsc(
            houseId, listOf(LedgerStatus.UNPAID, LedgerStatus.PARTIAL)
        )

    @Transactional(readOnly = true)
    fun getAllFines(houseId: Long): List<Fine> =
        fineRepository.findByHouseIdOrderByTriggerMonthAsc(houseId)
}
