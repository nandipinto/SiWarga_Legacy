package com.siwarga.repository

import com.siwarga.domain.entity.DuesLedger
import com.siwarga.domain.enums.LedgerStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface DuesLedgerRepository : JpaRepository<DuesLedger, Long> {

    fun findByHouseIdAndPeriodYearAndPeriodMonth(
        houseId: Long, year: Int, month: Int
    ): Optional<DuesLedger>

    fun findByHouseIdOrderByPeriodYearAscPeriodMonthAsc(houseId: Long): List<DuesLedger>

    fun findByHouseIdAndStatusInOrderByPeriodYearAscPeriodMonthAsc(
        houseId: Long, statuses: List<LedgerStatus>
    ): List<DuesLedger>

    @Query(
        "SELECT dl FROM DuesLedger dl WHERE dl.house.id = :houseId " +
        "AND (dl.periodYear < :year OR (dl.periodYear = :year AND dl.periodMonth <= :month)) " +
        "AND dl.status IN :statuses " +
        "ORDER BY dl.periodYear ASC, dl.periodMonth ASC"
    )
    fun findUnpaidUpToMonth(
        houseId: Long, year: Int, month: Int, statuses: List<LedgerStatus>
    ): List<DuesLedger>

    fun findByPeriodYearAndPeriodMonth(year: Int, month: Int): List<DuesLedger>

    @Query("SELECT dl FROM DuesLedger dl WHERE dl.house.rt.id = :rtId AND dl.periodYear = :year AND dl.periodMonth = :month")
    fun findByRtIdAndPeriodYearAndPeriodMonth(rtId: Long, year: Int, month: Int): List<DuesLedger>
}
