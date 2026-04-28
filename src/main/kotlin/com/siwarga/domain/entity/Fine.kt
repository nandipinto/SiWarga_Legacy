package com.siwarga.domain.entity

import com.siwarga.domain.enums.FineType
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
import java.time.LocalDateTime

@Entity
@Table(name = "fine")
class Fine(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    val house: House,
    @Enumerated(EnumType.STRING)
    @Column(name = "fine_type", nullable = false, length = 20)
    val fineType: FineType,
    @Column(nullable = false)
    val amount: Long,
    // 1-based consecutive-unpaid-month position from system start that triggered this fine
    @Column(name = "trigger_month", nullable = false)
    val triggerMonth: Int,
    @Column(name = "amount_paid", nullable = false)
    var amountPaid: Long = 0L,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: LedgerStatus = LedgerStatus.UNPAID,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
