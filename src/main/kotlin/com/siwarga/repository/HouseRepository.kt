package com.siwarga.repository

import com.siwarga.domain.entity.House
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface HouseRepository : JpaRepository<House, Long> {
    fun findByRtId(rtId: Long): List<House>
    fun findByRtIdAndBlockAndHouseNumber(rtId: Long, block: String, houseNumber: String): Optional<House>

    @Query("SELECT h FROM House h JOIN h.rt r WHERE r.name = :rtName AND h.block = :block AND h.houseNumber = :houseNumber")
    fun findByRtNameAndBlockAndHouseNumber(rtName: String, block: String, houseNumber: String): Optional<House>

    fun existsByRtIdAndBlockAndHouseNumber(rtId: Long, block: String, houseNumber: String): Boolean
}
