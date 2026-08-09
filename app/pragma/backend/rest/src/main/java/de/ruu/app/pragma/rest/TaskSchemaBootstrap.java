package de.ruu.app.pragma.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

final class TaskSchemaBootstrap
{
    private static final Logger LOG = LogManager.getLogger(TaskSchemaBootstrap.class);
    private static final AtomicBoolean PATCHED = new AtomicBoolean(false);

    private TaskSchemaBootstrap() { }

    static void ensureTaskColumns() throws SQLException, NamingException
    {
        if (!PATCHED.compareAndSet(false, true)) return;
        try
        {
            DataSource dataSource = (DataSource) new InitialContext().lookup("jdbc/pragma");
            try (Connection connection = dataSource.getConnection())
            {
                if (!tableExists(connection, "task")) return;

                // Simple dev-time safety net: keep the live schema aligned with the current task model
                // before Hibernate executes a query against the task table.
                ensureStatusColumn(connection);
                ensurePriorityColumn(connection);
            }
        }
        catch (SQLException e)
        {
            if (isRecoverableSchemaError(e))
            {
                LOG.warn("Skipping task schema bootstrap because the current database user cannot alter the task table", e);
                return;
            }
            PATCHED.set(false);
            throw e;
        }
        catch (NamingException e)
        {
            PATCHED.set(false);
            throw e;
        }
    }

    private static void ensureStatusColumn(Connection connection) throws SQLException
    {
        if (columnExists(connection, "task", "status"))
        {
            migrateStatusColumn(connection);
            return;
        }

        try
        {
            try (Statement statement = connection.createStatement())
            {
                statement.executeUpdate("ALTER TABLE task ADD COLUMN status varchar(32)");
            }
            try (Statement statement = connection.createStatement())
            {
                statement.executeUpdate("ALTER TABLE task ALTER COLUMN status SET DEFAULT 'NEW'");
                statement.executeUpdate("ALTER TABLE task ALTER COLUMN status SET NOT NULL");
            }
        }
        catch (SQLException e)
        {
            if (isRecoverableSchemaError(e))
            {
                LOG.warn("Unable to add or finalize the task status column; continuing without schema patching", e);
                return;
            }
            throw e;
        }

        migrateStatusColumn(connection);
    }

    private static void ensurePriorityColumn(Connection connection) throws SQLException
    {
        if (columnExists(connection, "task", "priority"))
        {
            migratePriorityColumn(connection);
            return;
        }

        try
        {
            try (Statement statement = connection.createStatement())
            {
                statement.executeUpdate("ALTER TABLE task ADD COLUMN priority varchar(32)");
            }
            try (Statement statement = connection.createStatement())
            {
                statement.executeUpdate("ALTER TABLE task ALTER COLUMN priority SET DEFAULT 'NORMAL'");
                statement.executeUpdate("ALTER TABLE task ALTER COLUMN priority SET NOT NULL");
            }
        }
        catch (SQLException e)
        {
            if (isRecoverableSchemaError(e))
            {
                LOG.warn("Unable to add or finalize the task priority column; continuing without schema patching", e);
                return;
            }
            throw e;
        }

        migratePriorityColumn(connection);
    }

    private static void migrateStatusColumn(Connection connection) throws SQLException
    {
        if (columnExists(connection, "task", "closed"))
        {
            try (Statement statement = connection.createStatement())
            {
                statement.executeUpdate("""
                    UPDATE task
                       SET status = CASE WHEN closed THEN 'CLOSED' ELSE 'NEW' END
                     WHERE status IS NULL
                    """);
            }
        }
        try (Statement statement = connection.createStatement())
        {
            statement.executeUpdate("UPDATE task SET status = 'NEW' WHERE status IS NULL");
        }
    }

    private static void migratePriorityColumn(Connection connection) throws SQLException
    {
        try (Statement statement = connection.createStatement())
        {
            statement.executeUpdate("UPDATE task SET priority = 'NORMAL' WHERE priority IS NULL");
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException
    {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, tableName, null))
        {
            return rs.next();
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException
    {
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, columnName))
        {
            return rs.next();
        }
    }

    static boolean isRecoverableSchemaError(SQLException e)
    {
        String message = e.getMessage();
        if (message == null) return false;
        String normalized = message.toLowerCase();
        return normalized.contains("must be owner of table")
            || normalized.contains("permission denied")
            || normalized.contains("does not have required privileges");
    }
}
