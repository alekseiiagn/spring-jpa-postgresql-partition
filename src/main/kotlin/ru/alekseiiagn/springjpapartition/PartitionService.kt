package ru.alekseiiagn.springjpapartition

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PartitionService(
    private val jdbcTemplate: JdbcTemplate
) {

    fun createUserOrderPartition(month: LocalDate) {
        jdbcTemplate.execute(
            """
                SELECT create_user_orders_partition('${month}'::date)
                """.trimIndent()
        )
    }
}