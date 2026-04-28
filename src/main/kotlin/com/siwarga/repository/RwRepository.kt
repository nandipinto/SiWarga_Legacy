package com.siwarga.repository

import com.siwarga.domain.entity.Rw
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RwRepository : JpaRepository<Rw, Long> {
    fun findByName(name: String): Optional<Rw>
    fun existsByName(name: String): Boolean
}
