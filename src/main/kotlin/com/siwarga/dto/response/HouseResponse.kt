package com.siwarga.dto.response

import com.siwarga.domain.entity.House

data class HouseResponse(
    val id: Long,
    val rtId: Long,
    val rtName: String,
    val block: String,
    val houseNumber: String,
    val displayName: String
) {
    companion object {
        fun from(house: House) = HouseResponse(
            id = house.id,
            rtId = house.rt.id,
            rtName = house.rt.name,
            block = house.block,
            houseNumber = house.houseNumber,
            displayName = house.displayName
        )
    }
}
