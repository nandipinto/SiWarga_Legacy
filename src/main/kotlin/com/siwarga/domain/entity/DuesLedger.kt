package com.siwarga.domain.entity

import com.siwarga.domain.enums.LedgerStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "dues_ledger",
    uniqueConstraints = [UniqueConstraint(columnNames = ["house_id", "period_year", "period_month"])],
)
class DuesLedger(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    val house: House,
    @Column(name = "period_year", nullable = false)
    val periodYear: Int,
    @Column(name = "period_month", nullable = false)
    val periodMonth: Int,
    @Column(name = "amount_due", nullable = false)
    val amountDue: Long = 120_000L,
    @Column(name = "amount_paid", nullable = false)
    var amountPaid: Long = 0L,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: LedgerStatus = LedgerStatus.UNPAID,
    @Column(name = "due_date", nullable = false)
    val dueDate: LocalDate,
    @Column(name = "paid_date")
    var paidDate: LocalDate? = null,
)
