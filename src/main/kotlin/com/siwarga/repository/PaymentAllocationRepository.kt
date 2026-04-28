package com.siwarga.repository

import com.siwarga.domain.entity.PaymentAllocation
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentAllocationRepository : JpaRepository<PaymentAllocation, Long> {
    fun findByPaymentId(paymentId: Long): List<PaymentAllocation>
}
