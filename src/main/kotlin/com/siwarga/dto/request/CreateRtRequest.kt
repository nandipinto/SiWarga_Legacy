package com.siwarga.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateRtRequest(
    @field:NotNull val rwId: Long,
    @field:NotBlank val name: String,
    val description: String? = null
)
