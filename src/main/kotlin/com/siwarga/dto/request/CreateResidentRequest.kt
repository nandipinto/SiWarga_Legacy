package com.siwarga.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateResidentRequest(
    @field:Positive val houseId: Long,
    @field:NotBlank val name: String,
    @field:Email val email: String? = null,
    val phone: String? = null
)
