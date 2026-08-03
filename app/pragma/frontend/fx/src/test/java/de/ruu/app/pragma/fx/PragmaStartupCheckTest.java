package de.ruu.app.pragma.fx;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PragmaStartupCheckTest
{
    @Test
    void formatFailureMessageListsAllServices()
    {
        List<PragmaStartupCheck.CheckResult> failures = List.of(
            new PragmaStartupCheck.CheckResult("PostgreSQL", "nicht erreichbar", "docker compose up -d", null),
            new PragmaStartupCheck.CheckResult("Keycloak", "antwortet nicht", "docker compose up -d", "timeout"),
            new PragmaStartupCheck.CheckResult("Backend-Health", "HTTP 503", "Logs pruefen", "{\"status\":\"DOWN\"}")
        );

        String message = PragmaStartupCheck.formatFailureMessage(failures);

        assertThat(message)
            .contains("Pragma kann noch nicht starten")
            .contains("1. PostgreSQL")
            .contains("Problem: nicht erreichbar")
            .contains("2. Keycloak")
            .contains("Details: timeout")
            .contains("3. Backend-Health")
            .contains("HTTP 503")
            .contains("Bitte die oben genannten Dienste starten");
    }
}
