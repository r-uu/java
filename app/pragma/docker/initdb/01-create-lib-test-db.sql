DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'pragma') THEN
        CREATE ROLE pragma LOGIN PASSWORD 'pragma';
    END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'keycloak') THEN
        CREATE ROLE keycloak LOGIN PASSWORD 'keycloak';
    END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'lib_test') THEN
        CREATE ROLE lib_test LOGIN PASSWORD 'lib_test';
    END IF;
END
$$;

SELECT 'CREATE DATABASE pragma OWNER pragma'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'pragma')\gexec

SELECT 'CREATE DATABASE keycloak OWNER keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

SELECT 'CREATE DATABASE lib_test OWNER lib_test'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lib_test')\gexec

GRANT ALL PRIVILEGES ON DATABASE pragma TO pragma;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
GRANT ALL PRIVILEGES ON DATABASE lib_test TO lib_test;
