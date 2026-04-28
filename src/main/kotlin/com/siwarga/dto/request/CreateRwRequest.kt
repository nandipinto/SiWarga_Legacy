package com.siwarga.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateRwRequest(
    @field:NotBlank val name: String,
    val description: String? = null
)
