package de.ruu.app.pragma.rest;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

final class UserSchemaBootstrap
{
    private static final AtomicBoolean PATCHED = new AtomicBoolean(false);

    private UserSchemaBootstrap() { }

    static void ensureDuplicateEmailsAllowed() throws SQLException, NamingException
    {
        if (!PATCHED.compareAndSet(false, true)) return;
        try
        {
            DataSource dataSource = (DataSource) new InitialContext().lookup("jdbc/pragma");
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement())
            {
                dropEmailUniqueConstraints(statement, connection);
            }
        }
        catch (SQLException | NamingException e)
        {
            PATCHED.set(false);
            throw e;
        }
    }

    private static void dropEmailUniqueConstraints(Statement statement, Connection connection) throws SQLException
    {
        try (PreparedStatement ps = connection.prepareStatement("""
            SELECT tc.constraint_name
              FROM information_schema.table_constraints tc
              JOIN information_schema.key_column_usage kcu
                ON tc.constraint_name = kcu.constraint_name
               AND tc.table_schema = kcu.table_schema
               AND tc.table_name = kcu.table_name
             WHERE tc.table_schema = current_schema()
               AND tc.table_name = 'app_user'
               AND tc.constraint_type = 'UNIQUE'
               AND kcu.column_name = 'email'
            """);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                String constraintName = rs.getString("constraint_name");
                if (constraintName == null || constraintName.isBlank()) continue;
                statement.executeUpdate("ALTER TABLE app_user DROP CONSTRAINT IF EXISTS " + quoteIdentifier(constraintName));
            }
        }
    }

    private static String quoteIdentifier(String identifier)
    {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }
}
