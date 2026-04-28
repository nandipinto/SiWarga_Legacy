package com.siwarga.repository

import com.siwarga.domain.entity.Fine
import com.siwarga.domain.enums.FineType
import com.siwarga.domain.enums.LedgerStatus
import org.springframework.data.jpa.repository.JpaRepository

interface FineRepository : JpaRepository<Fine, Long> {

    fun findByHouseIdOrderByTriggerMonthAsc(houseId: Long): List<Fine>

    fun findByHouseIdAndStatusInOrderByTriggerMonthAsc(
        houseId: Long, statuses: List<LedgerStatus>
    ): List<Fine>

    fun existsByHouseIdAndFineTypeAndTriggerMonth(
        houseId: Long, fineType: FineType, triggerMonth: Int
    ): Boolean
}
