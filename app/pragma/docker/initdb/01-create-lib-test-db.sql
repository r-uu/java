DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'lib_test') THEN
        CREATE ROLE lib_test LOGIN PASSWORD 'lib_test';
    END IF;
END
$$;

SELECT 'CREATE DATABASE lib_test OWNER lib_test'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lib_test')\gexec

GRANT ALL PRIVILEGES ON DATABASE lib_test TO lib_test;
