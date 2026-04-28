package com.siwarga.dto.response

import com.siwarga.domain.entity.Resident

data class ResidentResponse(
    val id: Long,
    val houseId: Long,
    val houseDisplay: String,
    val rtName: String,
    val name: String,
    val email: String?,
    val phone: String?
) {
    companion object {
        fun from(resident: Resident) = ResidentResponse(
            id = resident.id,
            houseId = resident.house.id,
            houseDisplay = resident.house.displayName,
            rtName = resident.house.rt.name,
            name = resident.name,
            email = resident.email,
            phone = resident.phone
        )
    }
}
