package com.siwarga.dto.response

import com.siwarga.domain.entity.Rw
import java.time.LocalDateTime

data class RwResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(rw: Rw) = RwResponse(rw.id, rw.name, rw.description, rw.createdAt)
    }
}
