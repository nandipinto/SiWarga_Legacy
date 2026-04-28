package com.siwarga.repository

import com.siwarga.domain.entity.Rt
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RtRepository : JpaRepository<Rt, Long> {
    fun findByName(name: String): Optional<Rt>
    fun existsByName(name: String): Boolean
}
