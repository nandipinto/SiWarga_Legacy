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
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "house",
    uniqueConstraints = [UniqueConstraint(columnNames = ["rt_id", "block", "house_number"])],
)
class House(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rt_id", nullable = false)
    var rt: Rt,
    @Column(nullable = false, length = 20)
    var block: String,
    @Column(name = "house_number", nullable = false, length = 20)
    var houseNumber: String,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    val displayName: String get() = "Blok $block No. $houseNumber"
}
