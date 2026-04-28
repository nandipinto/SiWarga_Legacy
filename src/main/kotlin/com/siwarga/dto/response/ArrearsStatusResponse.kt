package com.siwarga.dto.response

data class ArrearsStatusResponse(
    val houseId: Long,
    val houseDisplay: String,
    val rtName: String,
    val residentName: String?,
    val unpaidMonths: List<String>,         // e.g. ["2024-01", "2024-02"]
    val totalDuesOwed: Long,
    val totalFinesOwed: Long,
    val totalOwed: Long,
    val fineBreakdown: List<FineDetail>
)

data class FineDetail(
    val id: Long,
    val fineType: String,
    val amount: Long,
    val amountPaid: Long,
    val amountOwed: Long,
    val triggerMonth: Int,
    val status: String
)
