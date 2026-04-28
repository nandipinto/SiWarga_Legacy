package com.siwarga.dto.response

import com.siwarga.domain.entity.Rt
import java.time.LocalDateTime

data class RtResponse(
    val id: Long,
    val rwId: Long,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(rt: Rt) = RtResponse(rt.id, rt.rw.id, rt.name, rt.description, rt.createdAt)
    }
}
