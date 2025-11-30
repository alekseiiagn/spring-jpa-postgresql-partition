package ru.alekseiiagn.springjpapartition

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.random.Random


@EnableScheduling
@SpringBootApplication
class SpringJpaPartitionApplication(
    private val partitionService: PartitionService,
    private val userOrderRepository: UserOrderRepository,
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val now = LocalDate.now()
        val prevMonth = now.minus(1, ChronoUnit.MONTHS)
        val prev2Month = now.minus(2, ChronoUnit.MONTHS)

        partitionService.createUserOrderPartition(now)
        partitionService.createUserOrderPartition(prevMonth)
        partitionService.createUserOrderPartition(prev2Month)

        userOrderRepository.save(generateUserOrder(now))
        userOrderRepository.save(generateUserOrder(prevMonth))
        userOrderRepository.save(generateUserOrder(prev2Month))
    }

    private fun generateUserOrder(date: LocalDate): UserOrder = UserOrder(
        userId = Random.nextLong(1_000, 10_000),
        amount = Random.nextDouble(1_000.0, 3_000.0),
        createdAt = date
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
    )
}

fun main(args: Array<String>) {
    runApplication<SpringJpaPartitionApplication>(*args)
}

