# PostgreSQL Partitioning (Spring Boot + JPA + Liquibase)

This project demonstrates how to implement **table partitioning in PostgreSQL 13+** using Spring Boot, JPA, and
Liquibase. The example shows monthly range partitioning for a `user_orders` table, which is a common pattern for
time-series data.

## Features

- Monthly range partitioning for time-series data
- Automatic partition creation via [
  `Scheduled Task`](src/main/kotlin/ru/alekseiiagn/springjpapartition/PartitionScheduler.kt)
- Automatic index creation on partitions
- Liquibase migration management

## Project Structure

```
src/main/
├── kotlin/ru/alekseiiagn/springjpapartition/
│   ├── SpringJpaPartitionApplication.kt                # Main application with demo data
│   ├── UserOrder.kt                                    # JPA entity
│   ├── UserOrderRepository.kt                          # Spring Data JPA repository
│   ├── PartitionService.kt                             # Service for partition management
│   └── PartitionScheduler.kt                           # Scheduled partition creation
└── resources/
    ├── application.yaml                                # Spring Boot configuration
    └── db/changelog/
        ├── db.changelog-master.yaml                    # Liquibase master changelog
        ├── user_orders.sql                             # Main partitioned table definition
        └── create_partition_functions.sql   # Partition creation functions
```

## How It Works

### 1. Partitioned Table Definition

The main table is defined in [`user_orders.sql`](src/main/resources/db/changelog/user_orders.sql):

```sql
CREATE TABLE user_orders
(
    order_id   bigserial,
    user_id    bigint      NOT NULL,
    amount     numeric     NOT NULL,
    created_at timestamptz NOT NULL,

    CONSTRAINT user_orders_pkey PRIMARY KEY (created_at, order_id)
) PARTITION BY RANGE (created_at);
```

**Key points:**

- The table is partitioned by `RANGE` on the `created_at` column
- The primary key **must include the partition key** (`created_at`) - this is a PostgreSQL requirement

### 2. Partition Creation Functions

The partition creation logic is implemented in [
`create_partition_functions.sql`](src/main/resources/db/changelog/create_partition_functions.sql):

- #### `table_exists()` :
  A utility function to check if a table (partition) already exists in the current schema.

- #### `create_user_orders_partition()` :
  This function:
    - Generates a partition name in the format `user_orders_YYYY-MM`
    - Checks if the partition already exists
    - Automatically creates indexes on `created_at` and `order_id` columns

### 3. Spring Boot Integration

#### PartitionService

[`PartitionService.kt`](src/main/kotlin/ru/alekseiiagn/springjpapartition/PartitionService.kt) provides a Java/Kotlin
interface to create partitions:

```kotlin
fun createUserOrderPartition(month: LocalDate) {
    jdbcTemplate.execute(
        "SELECT create_user_orders_partition('${month}'::date)"
    )
}
```

#### PartitionScheduler

[`PartitionScheduler.kt`](src/main/kotlin/ru/alekseiiagn/springjpapartition/PartitionScheduler.kt) automatically creates
partitions for the next month on the 3rd day of each month at 3:00 AM:

```kotlin
@Scheduled(cron = "0 0 3 3 * ?")
fun scheduleUserOrderNextMonthPartition() {
    val nextMonth = createNextMonthDate()
    partitionService.createUserOrderPartition(nextMonth)
}
```

## Setup and Usage

### Prerequisites

- Java 21+
- PostgreSQL 13 or higher
- Gradle 7+

### Configuration

1. Set up your PostgreSQL connection in `application.yaml` or via environment variables:
   ```yaml
   spring:
     datasource:
       url: ${POSTGRES_URL}
       username: ${POSTGRES_USER}
       password: ${POSTGRES_PASSWORD}
   ```

2. Run the application - Liquibase will automatically:
    - Create the partitioned table
    - Create the partition management functions

3. The application will automatically create partitions for the **current month** and **previous months**, and **insert
   sample data** for example.

## Additional

### Querying Partitioned Tables

PostgreSQL automatically routes queries to the appropriate partitions. You can query the main table normally:

```kotlin
// Spring Data JPA - works automatically
userOrderRepository.findByCreatedAtBetween(startDate, endDate)
```

```sql
-- Direct SQL - PostgreSQL handles partition pruning
SELECT *
FROM user_orders
WHERE created_at >= '2024-01-01'
  AND created_at < '2024-02-01';
```

PostgreSQL will automatically use partition pruning to only scan relevant partitions.

## References

- [PostgreSQL Partitioning Documentation](https://www.postgresql.org/docs/current/ddl-partitioning.html)
- [PostgreSQL Range Partitioning](https://www.postgresql.org/docs/current/ddl-partitioning.html#DDL-PARTITIONING-RANGE)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
