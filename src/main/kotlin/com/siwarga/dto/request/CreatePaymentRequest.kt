package com.siwarga.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class CreatePaymentRequest(
    @field:Positive val houseId: Long,
    @field:Positive val amount: Long,
    @field:NotNull val paymentDate: LocalDate,
    val receiptNumber: String? = null,
    val notes: String? = null
)
