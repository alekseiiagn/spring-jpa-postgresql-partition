package ru.alekseiiagn.springjpapartition

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PartitionScheduler(
    private val partitionService: PartitionService
) {

    @Scheduled(cron = "0 0 3 3 * ?")
    fun scheduleUserOrderNextMonthPartition() {
        val nextMonth = createNextMonthDate()
        partitionService.createUserOrderPartition(nextMonth)
    }

    private fun createNextMonthDate(): LocalDate =
        LocalDate.now()
            .withDayOfMonth(1)
            .plusMonths(1)
}
