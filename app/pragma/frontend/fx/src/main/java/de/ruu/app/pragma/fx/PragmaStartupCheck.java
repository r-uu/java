package de.ruu.app.pragma.fx;

import de.ruu.lib.util.IO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class PragmaStartupCheck
{
    private static final Logger log = LogManager.getLogger(PragmaStartupCheck.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private PragmaStartupCheck() { }

    static void verify()
    {
        List<CheckResult> failures = run();
        if (failures.isEmpty())
        {
            log.info("Pragma startup check passed");
            return;
        }

        String message = formatFailureMessage(failures);
        log.error(message);
        throw new IllegalStateException(message);
    }

    static List<CheckResult> run()
    {
        List<CheckResult> failures = new ArrayList<>();

        checkPostgreSQL(failures);
        checkKeycloak(failures);
        checkBackend(failures);

        return List.copyOf(failures);
    }

    static String formatFailureMessage(List<CheckResult> failures)
    {
        StringBuilder message = new StringBuilder();
        message.append("Pragma kann noch nicht starten, weil mindestens ein benoetigter Dienst fehlt oder nicht bereit ist:\n");

        for (int i = 0; i < failures.size(); i++)
        {
            CheckResult failure = failures.get(i);
            if (i > 0) message.append('\n');

            message.append(i + 1).append(". ").append(failure.service()).append('\n');
            message.append("   Problem: ").append(failure.problem()).append('\n');
            if (failure.details() != null && !failure.details().isBlank())
            {
                message.append("   Details: ").append(failure.details().trim()).append('\n');
            }
            message.append("   Hinweis: ").append(failure.hint());
        }

        message.append("\n\nBitte die oben genannten Dienste starten und Pragma dann erneut aufrufen.");
        return message.toString();
    }

    private static void checkPostgreSQL(List<CheckResult> failures)
    {
        String host = value("pragma.postgres.host", "localhost");
        int port = intValue("pragma.postgres.port", 5432);

        if (isListening(host, port))
        {
            return;
        }

        failures.add(failure(
            "PostgreSQL",
            "Der PostgreSQL-Dienst auf " + host + ":" + port + " antwortet nicht.",
            "Pruefe den Container pragma-postgres oder starte die Docker-Services mit `docker compose up -d`."
        ));
    }

    private static void checkKeycloak(List<CheckResult> failures)
    {
        String serverUrl = value("pragma.keycloak.server-url", "http://localhost:8080");
        URI baseUri;

        try
        {
            baseUri = URI.create(trimTrailingSlash(serverUrl));
        }
        catch (IllegalArgumentException e)
        {
            failures.add(failure(
                "Keycloak",
                "Die Keycloak-Server-URL ist ungueltig: " + serverUrl,
                "Pruefe die Property pragma.keycloak.server-url.",
                e.getMessage()
            ));
            return;
        }

        String host = baseUri.getHost();
        int port = portOrDefault(baseUri);

        if (host == null || host.isBlank())
        {
            failures.add(failure(
                "Keycloak",
                "Die Keycloak-Server-URL enthaelt keinen Host: " + serverUrl,
                "Pruefe die Property pragma.keycloak.server-url."
            ));
            return;
        }

        if (!isListening(host, port))
        {
            failures.add(failure(
                "Keycloak",
                "Der Keycloak-Server auf " + host + ":" + port + " antwortet nicht.",
                "Pruefe den Container pragma-keycloak oder starte die Docker-Services mit `docker compose up -d`."
            ));
            return;
        }

        String realm = value("pragma.keycloak.realm", "pragma-realm");
        URI realmUri = URI.create(trimTrailingSlash(serverUrl) + "/realms/" + realm + "/.well-known/openid-configuration");

        try
        {
            HttpResponse<String> response = sendGet(realmUri);
            if (response.statusCode() == 200)
            {
                return;
            }

            String hint = response.statusCode() == 404
                ? "Pruefe, ob der Realm pragma-realm bereits angelegt wurde."
                : "Pruefe die Keycloak-Logs und die Realm-Konfiguration.";

            failures.add(failure(
                "Keycloak-Realm",
                "Der Keycloak-Realm '" + realm + "' ist nicht verfuegbar (HTTP " + response.statusCode() + ").",
                hint,
                bodySnippet(response.body())
            ));
        }
        catch (IOException e)
        {
            failures.add(failure(
                "Keycloak-Realm",
                "Der Keycloak-Realm '" + realm + "' konnte nicht abgefragt werden.",
                "Pruefe die Keycloak-Logs und die Realm-Konfiguration.",
                e.getMessage()
            ));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            failures.add(failure(
                "Keycloak-Realm",
                "Die Pruefung des Keycloak-Realm wurde unterbrochen.",
                "Bitte den Start erneut ausfuehren."
            ));
        }
    }

    private static void checkBackend(List<CheckResult> failures)
    {
        String host = value("pragma.rest-api.host", "localhost");
        int port = intValue("pragma.rest-api.port", 9090);

        if (!isListening(host, port))
        {
            failures.add(failure(
                "Backend",
                "Der Pragma-Backend-Server auf " + host + ":" + port + " antwortet nicht.",
                "Starte das Backend mit `mvn -pl backend/rest package liberty:dev`."
            ));
            return;
        }

        try
        {
            HttpResponse<String> response = sendGet(URI.create("http://" + host + ":" + port + "/health/ready"));
            if (response.statusCode() == 404)
            {
                response = sendGet(URI.create("http://" + host + ":" + port + "/pragma/health/ready"));
            }
            if (response.statusCode() == 200)
            {
                return;
            }

            failures.add(failure(
                "Backend-Health",
                "Der Backend-Health-Endpunkt meldet HTTP " + response.statusCode() + ".",
                "Pruefe die Backend-Logs und die Fehlermeldungen der anderen Checks.",
                bodySnippet(response.body())
            ));
        }
        catch (IOException e)
        {
            failures.add(failure(
                "Backend-Health",
                "Der Backend-Health-Endpunkt konnte nicht gelesen werden.",
                "Pruefe die Backend-Logs und die Fehlermeldungen der anderen Checks.",
                e.getMessage()
            ));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            failures.add(failure(
                "Backend-Health",
                "Die Pruefung des Backend-Health-Endpunkts wurde unterbrochen.",
                "Bitte den Start erneut ausfuehren."
            ));
        }
    }

    private static HttpResponse<String> sendGet(URI uri) throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String value(String propertyName, String defaultValue)
    {
        String systemValue = System.getProperty(propertyName);
        if (systemValue != null && !systemValue.isBlank())
        {
            return systemValue.trim();
        }

        String envValue = System.getenv(envName(propertyName));
        if (envValue != null && !envValue.isBlank())
        {
            return envValue.trim();
        }

        return defaultValue;
    }

    private static String envName(String propertyName)
    {
        return propertyName.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
    }

    private static int intValue(String propertyName, int defaultValue)
    {
        String value = value(propertyName, Integer.toString(defaultValue));
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalStateException("Ungueltiger Wert fuer " + propertyName + ": " + value, e);
        }
    }

    private static String trimTrailingSlash(String value)
    {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static int portOrDefault(URI uri)
    {
        if (uri.getPort() != -1)
        {
            return uri.getPort();
        }

        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static String bodySnippet(String body)
    {
        if (body == null || body.isBlank())
        {
            return "";
        }

        String snippet = body.replaceAll("\\s+", " ").trim();
        if (snippet.length() <= 400)
        {
            return snippet;
        }
        return snippet.substring(0, 397) + "...";
    }

    private static boolean isListening(String host, int port)
    {
        try
        {
            return IO.isListening(host, port);
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private static CheckResult failure(String service, String problem, String hint)
    {
        return new CheckResult(service, problem, hint, null);
    }

    private static CheckResult failure(String service, String problem, String hint, String details)
    {
        return new CheckResult(service, problem, hint, details);
    }

    record CheckResult(String service, String problem, String hint, String details) { }
}
