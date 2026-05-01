package com.siwarga.repository

import com.siwarga.domain.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByHouseIdOrderByPaymentDateDesc(houseId: Long): List<Payment>

    fun existsByReceiptNumber(receiptNumber: String): Boolean

    fun findByReceiptNumber(receiptNumber: String): Optional<Payment>
}
