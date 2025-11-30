--liquibase formatted sql

-- changeset alekseiiagn:- labels:orders splitStatements:false


-- CHECK TABLE EXISTS FUNC ------------------------------------------------------------------------
-- A utility function to check if a table (partition) already exists in the current schema
CREATE OR REPLACE FUNCTION table_exists(p_schema_name text, p_table_name text)
    RETURNS boolean
AS
$func$
BEGIN
    RETURN to_regclass(format('%I.%I', p_schema_name, p_table_name)) IS NOT NULL;
END;
$func$ LANGUAGE plpgsql;


-- CREATE PARTITION FUNC ------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION create_user_orders_partition(p_month_start date)
    RETURNS void AS
$func$
DECLARE
    v_month_start    date := date_trunc('month', p_month_start)::date;
    v_month_end      date := (v_month_start + interval '1 month')::date;

    v_partition_name text := format('user_orders_%s', to_char(v_month_start, 'YYYY-MM'));
BEGIN

    -- Check if partition already exists
    IF table_exists(current_schema(), v_partition_name) THEN
        RETURN;
    END IF;

    -- Create new partition
    EXECUTE format(
            'CREATE TABLE %I PARTITION OF user_orders
               FOR VALUES FROM (%L) TO (%L);',
            v_partition_name,
            v_month_start,
            v_month_end
            );

    -- Create index on created_at
    EXECUTE format(
            'CREATE INDEX %I ON %I (created_at);',
            v_partition_name || '_created_at_idx',
            v_partition_name
            );

    -- Create index on order_id
    EXECUTE format(
            'CREATE INDEX %I ON %I (order_id);',
            v_partition_name || '_order_id_idx',
            v_partition_name
            );
END;
$func$ LANGUAGE plpgsql;