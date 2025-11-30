--liquibase formatted sql

-- changeset alekseiiagn:6 labels:boxes

DROP TABLE IF EXISTS user_orders CASCADE;

CREATE TABLE user_orders
(
    order_id   bigserial,
    user_id    bigint      NOT NULL,
    amount     numeric     NOT NULL,
    created_at timestamptz NOT NULL,

    CONSTRAINT user_orders_pkey PRIMARY KEY (created_at, order_id)
) PARTITION BY RANGE (created_at);
