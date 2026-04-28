package com.siwarga.repository

import com.siwarga.domain.entity.Resident
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ResidentRepository : JpaRepository<Resident, Long> {
    fun findByHouseId(houseId: Long): Optional<Resident>
    fun existsByHouseId(houseId: Long): Boolean
}
