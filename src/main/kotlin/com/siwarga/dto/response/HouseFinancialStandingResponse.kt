package com.siwarga.dto.response

data class HouseFinancialStandingResponse(
    val houseId: Long,
    val houseDisplay: String,
    val rtName: String,
    val residentName: String?,

    /** Sum of amountPaid on ledger entries for months beyond the current month. */
    val prepaidBalance: Long,

    /** Unpaid or partially-paid months up to and including the current month. */
    val arrears: List<ArrearDetail>,
    val totalArrearsOwed: Long,

    /** Unpaid or partially-paid fines. */
    val fines: List<FineDetail>,
    val totalFinesOwed: Long,

    /**
     * Positive → resident owes this amount.
     * Negative → resident has this amount in credit.
     * Formula: totalArrearsOwed + totalFinesOwed - prepaidBalance
     */
    val netBalance: Long,
)

data class ArrearDetail(
    val period: String,       // "YYYY-MM"
    val amountDue: Long,
    val amountPaid: Long,
    val amountOwed: Long,
    val status: String,
)
