package com.siwarga.dto.response

data class MonthlyReportResponse(
    val year: Int,
    val month: Int,
    val totalHouses: Int,
    val totalPaid: Int,
    val totalPartial: Int,
    val totalUnpaid: Int,
    val totalCollected: Long,
    val rows: List<MonthlyReportRow>
)

data class MonthlyReportRow(
    val houseId: Long,
    val rtName: String,
    val block: String,
    val houseNumber: String,
    val residentName: String?,
    val status: String,
    val amountDue: Long,
    val amountPaid: Long,
    val paidDate: String?
)
