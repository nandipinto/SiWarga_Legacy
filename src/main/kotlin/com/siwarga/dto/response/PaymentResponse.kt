package com.siwarga.dto.response

import com.siwarga.domain.entity.Payment
import java.time.LocalDate
import java.time.LocalDateTime

data class PaymentResponse(
    val id: Long,
    val houseId: Long,
    val houseDisplay: String,
    val amount: Long,
    val paymentDate: LocalDate,
    val receiptNumber: String?,
    val notes: String?,
    val recordedBy: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(payment: Payment) = PaymentResponse(
            id = payment.id,
            houseId = payment.house.id,
            houseDisplay = payment.house.displayName,
            amount = payment.amount,
            paymentDate = payment.paymentDate,
            receiptNumber = payment.receiptNumber,
            notes = payment.notes,
            recordedBy = payment.recordedBy?.username,
            createdAt = payment.createdAt
        )
    }
}
