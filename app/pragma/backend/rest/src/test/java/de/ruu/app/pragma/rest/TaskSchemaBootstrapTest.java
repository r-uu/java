package de.ruu.app.pragma.rest;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class TaskSchemaBootstrapTest
{
    @Test
    void ownerErrorsAreTreatAsRecoverable()
    {
        SQLException exception = new SQLException("ERROR: must be owner of table task");

        assertThat(TaskSchemaBootstrap.isRecoverableSchemaError(exception)).isTrue();
    }

    @Test
    void permissionErrorsAreTreatAsRecoverable()
    {
        SQLException exception = new SQLException("ERROR: permission denied for table task");

        assertThat(TaskSchemaBootstrap.isRecoverableSchemaError(exception)).isTrue();
    }

    @Test
    void unrelatedErrorsRemainNonRecoverable()
    {
        SQLException exception = new SQLException("ERROR: relation \"task\" does not exist");

        assertThat(TaskSchemaBootstrap.isRecoverableSchemaError(exception)).isFalse();
    }
}
