package com.siwarga.dto.response

data class PaymentAllocationResult(
    val paymentId: Long,
    val houseId: Long,
    val houseDisplay: String,
    val totalAmount: Long,
    val discountApplied: Long,
    val effectiveAmount: Long,
    val allocations: List<AllocationDetail>,
    val remainingUnallocated: Long,
)

data class AllocationDetail(
    val type: String, // "DUES" or "FINE"
    val period: String?, // "2024-06" for dues, null for fines
    val fineType: String?, // "SIX_MONTH" or "TWELVE_MONTH", null for dues
    val amountApplied: Long,
    val fullyPaid: Boolean,
)
