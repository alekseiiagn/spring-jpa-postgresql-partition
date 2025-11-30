package ru.alekseiiagn.springjpapartition

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "user_orders")
data class UserOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val orderId: Long = 0,

    @Column(nullable = false)
    val userId: Long = 0,

    @Column(nullable = false)
    val amount: Double = 0.0,

    @Column(nullable = false)
    val createdAt: Instant = Instant.EPOCH
)
