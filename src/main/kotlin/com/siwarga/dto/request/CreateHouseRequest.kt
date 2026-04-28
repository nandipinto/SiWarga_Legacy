package com.siwarga.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateHouseRequest(
    @field:Positive val rtId: Long,
    @field:NotBlank val block: String,
    @field:NotBlank val houseNumber: String
)
