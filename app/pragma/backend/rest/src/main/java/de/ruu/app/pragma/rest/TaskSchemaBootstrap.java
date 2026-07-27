package de.ruu.app.pragma.rest;

import org.jspecify.annotations.Nullable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

final class TaskSchemaBootstrap
{
    private static final AtomicBoolean PATCHED = new AtomicBoolean(false);

    private TaskSchemaBootstrap() { }

    static void ensureTaskColumns() throws SQLException, NamingException
    {
        if (!PATCHED.compareAndSet(false, true)) return;
        try
        {
            DataSource dataSource = (DataSource) new InitialContext().lookup("jdbc/pragma");
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement())
            {
                // Simple dev-time safety net: keep the live schema aligned with the current task model
                // before Hibernate executes a query against the task table.
                ensureStatusColumn(statement, connection);
                ensurePriorityColumn(statement);
            }
        }
        catch (SQLException | NamingException e)
        {
            PATCHED.set(false);
            throw e;
        }
    }

    private static void ensureStatusColumn(Statement statement, Connection connection) throws SQLException
    {
        statement.executeUpdate("ALTER TABLE task ADD COLUMN IF NOT EXISTS status varchar(32)");
        if (columnExists(connection, "task", "closed"))
        {
            statement.executeUpdate("""
                UPDATE task
                   SET status = CASE WHEN closed THEN 'CLOSED' ELSE 'NEW' END
                 WHERE status IS NULL
                """);
        }
        statement.executeUpdate("UPDATE task SET status = 'NEW' WHERE status IS NULL");
        statement.executeUpdate("ALTER TABLE task ALTER COLUMN status SET DEFAULT 'NEW'");
        statement.executeUpdate("ALTER TABLE task ALTER COLUMN status SET NOT NULL");
    }

    private static void ensurePriorityColumn(Statement statement) throws SQLException
    {
        statement.executeUpdate("ALTER TABLE task ADD COLUMN IF NOT EXISTS priority varchar(32)");
        statement.executeUpdate("UPDATE task SET priority = 'NORMAL' WHERE priority IS NULL");
        statement.executeUpdate("ALTER TABLE task ALTER COLUMN priority SET DEFAULT 'NORMAL'");
        statement.executeUpdate("ALTER TABLE task ALTER COLUMN priority SET NOT NULL");
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException
    {
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, columnName))
        {
            return rs.next();
        }
    }
}
