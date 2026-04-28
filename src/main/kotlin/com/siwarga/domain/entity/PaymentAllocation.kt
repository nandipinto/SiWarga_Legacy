package com.siwarga.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "payment_allocation")
class PaymentAllocation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    val payment: Payment,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_id")
    val ledger: DuesLedger? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fine_id")
    val fine: Fine? = null,
    @Column(name = "amount_applied", nullable = false)
    val amountApplied: Long,
) {
    init {
        require((ledger != null) xor (fine != null)) {
            "Exactly one of ledger or fine must be non-null"
        }
    }
}
