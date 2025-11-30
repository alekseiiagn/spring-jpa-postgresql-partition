package ru.alekseiiagn.springjpapartition

import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface UserOrderRepository : JpaRepository<UserOrder, Long> {
    fun findByCreatedAtBetween(createdAtAfter: Instant, createdAtBefore: Instant): MutableList<UserOrder>
}
