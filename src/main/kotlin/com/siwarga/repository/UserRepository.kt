package com.siwarga.repository

import com.siwarga.domain.entity.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<AppUser, Long> {
    fun findByUsername(username: String): Optional<AppUser>

    fun existsByUsername(username: String): Boolean
}
