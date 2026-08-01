package de.ruu.app.pragma.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ruu.app.pragma.dto.UserDto;
import de.ruu.app.pragma.jpa.UserJPA;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class KeycloakUserSyncService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private KeycloakUserSyncService()
    {
    }

    static int syncUsers(EntityManager em)
    {
        KeycloakConfig config = KeycloakConfig.read();
        List<KeycloakUser> realmUsers = fetchRealmUsers(config);
        Map<String, UserJPA> byKeycloakId = findExistingByKeycloakId(em);
        int synced = 0;
        for (KeycloakUser realmUser : realmUsers) {
            upsertLocalUser(em, realmUser, byKeycloakId);
            synced++;
        }
        return synced;
    }

    static UserJPA createInKeycloakAndSync(EntityManager em, UserDto dto)
    {
        KeycloakConfig config = KeycloakConfig.read();
        if (dto.username().isBlank()) throw new IllegalArgumentException("username is required");
        if (dto.email().isBlank()) throw new IllegalArgumentException("email is required");
        KeycloakUser created = createKeycloakUser(
            config,
            dto.username(),
            dto.displayName(),
            dto.email(),
            dto.active(),
            dto.password().orElse(null));
        upsertLocalUser(em, created, findExistingByKeycloakId(em));
        return findByKeycloakId(em, created.id())
            .orElseThrow(() -> new IllegalStateException("User sync failed for Keycloak user " + created.id()));
    }

    static UserJPA updateInKeycloakAndSync(EntityManager em, UserJPA localUser, UserDto dto)
    {
        String keycloakUserId = localUser.keycloakUserId()
            .orElseThrow(() -> new IllegalStateException("Local user has no keycloakUserId"));
        KeycloakConfig config = KeycloakConfig.read();
        updateKeycloakUser(
            config,
            keycloakUserId,
            dto.username(),
            dto.displayName(),
            dto.email(),
            dto.active(),
            dto.password().orElse(null));
        KeycloakUser refreshed = fetchOneKeycloakUser(config, keycloakUserId);
        upsertLocalUser(em, refreshed, findExistingByKeycloakId(em));
        return findByKeycloakId(em, keycloakUserId)
            .orElseThrow(() -> new IllegalStateException("User sync failed for Keycloak user " + keycloakUserId));
    }

    static void deleteInKeycloakAndLocal(EntityManager em, UserJPA localUser)
    {
        String keycloakUserId = localUser.keycloakUserId()
            .orElseThrow(() -> new IllegalStateException("Local user has no keycloakUserId"));
        KeycloakConfig config = KeycloakConfig.read();
        deleteKeycloakUser(config, keycloakUserId);
        em.remove(localUser);
    }

    private static List<KeycloakUser> fetchRealmUsers(KeycloakConfig config)
    {
        try (Client client = ClientBuilder.newBuilder().build()) {
            String token = obtainToken(client, config);
            try (Response response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users")
                .queryParam("max", 1000)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token)
                .get()) {
                if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL)
                    throw new IllegalStateException("Failed to fetch Keycloak users: HTTP " + response.getStatus());
                return readUsers(response.readEntity(String.class));
            }
        }
    }

    private static KeycloakUser createKeycloakUser(
        KeycloakConfig config,
        String username,
        String displayName,
        String email,
        boolean enabled,
        @Nullable String password)
    {
        try (Client client = ClientBuilder.newBuilder().build()) {
            String token = obtainToken(client, config);
            String userId;
            try (Response response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token)
                .post(Entity.entity(userBody(username, displayName, email, enabled), MediaType.APPLICATION_JSON_TYPE))) {
                if (response.getStatus() != 201) throw new IllegalStateException("Failed to create Keycloak user: HTTP " + response.getStatus());
                String location = response.getHeaderString("Location");
                if (location == null || location.isBlank()) throw new IllegalStateException("Missing Location header from Keycloak create user response");
                userId = location.substring(location.lastIndexOf('/') + 1);
            }
            if (password != null && !password.isBlank()) resetPassword(client, config, token, userId, password);
            return fetchOneKeycloakUser(config, userId);
        }
    }

    private static void updateKeycloakUser(
        KeycloakConfig config,
        String userId,
        String username,
        String displayName,
        String email,
        boolean enabled,
        @Nullable String password)
    {
        try (Client client = ClientBuilder.newBuilder().build()) {
            String token = obtainToken(client, config);
            try (Response response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId)
                .request()
                .header("Authorization", "Bearer " + token)
                .put(Entity.entity(userBody(username, displayName, email, enabled), MediaType.APPLICATION_JSON_TYPE))) {
                if (response.getStatus() != 204) throw new IllegalStateException("Failed to update Keycloak user: HTTP " + response.getStatus());
            }
            if (password != null && !password.isBlank()) resetPassword(client, config, token, userId, password);
        }
    }

    private static void deleteKeycloakUser(KeycloakConfig config, String userId)
    {
        try (Client client = ClientBuilder.newBuilder().build()) {
            String token = obtainToken(client, config);
            try (Response response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId)
                .request()
                .header("Authorization", "Bearer " + token)
                .delete()) {
                if (response.getStatus() != 204) throw new IllegalStateException("Failed to delete Keycloak user: HTTP " + response.getStatus());
            }
        }
    }

    private static KeycloakUser fetchOneKeycloakUser(KeycloakConfig config, String userId)
    {
        try (Client client = ClientBuilder.newBuilder().build()) {
            String token = obtainToken(client, config);
            try (Response response = client
                .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token)
                .get()) {
                if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL)
                    throw new IllegalStateException("Failed to fetch Keycloak user " + userId + ": HTTP " + response.getStatus());
                return toKeycloakUser(readObject(response.readEntity(String.class)));
            }
        }
    }

    private static String obtainToken(Client client, KeycloakConfig config)
    {
        Form form = new Form()
            .param("grant_type", "password")
            .param("client_id", config.clientId())
            .param("username", config.adminUsername())
            .param("password", config.adminPassword());
        try (Response response = client
            .target(config.serverUrl() + "/realms/master/protocol/openid-connect/token")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE))) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL)
                throw new IllegalStateException("Failed to obtain Keycloak token: HTTP " + response.getStatus());
            Map<String, Object> payload = readObject(response.readEntity(String.class));
            Object accessToken = payload.get("access_token");
            if (!(accessToken instanceof String token) || token.isBlank())
                throw new IllegalStateException("Keycloak token response does not contain access_token");
            return token;
        }
    }

    private static void resetPassword(Client client, KeycloakConfig config, String token, String userId, String password)
    {
        Map<String, Object> body = Map.of("type", "password", "value", password, "temporary", false);
        try (Response response = client
            .target(config.serverUrl() + "/admin/realms/" + config.realm() + "/users/" + userId + "/reset-password")
            .request()
            .header("Authorization", "Bearer " + token)
            .put(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE))) {
            if (response.getStatus() != 204) throw new IllegalStateException("Failed to reset Keycloak password: HTTP " + response.getStatus());
        }
    }

    private static void upsertLocalUser(EntityManager em, KeycloakUser realmUser, Map<String, UserJPA> byKeycloakId)
    {
        UserJPA entity = byKeycloakId.get(realmUser.id());
        if (entity == null) entity = findByUsername(em, realmUser.username()).orElse(null);
        if (entity == null) {
            entity = new UserJPA(realmUser.username(), realmUser.displayName(), realmUser.email());
            entity.keycloakUserId(realmUser.id());
            entity.active(realmUser.enabled());
            em.persist(entity);
        } else {
            entity.username(realmUser.username());
            entity.displayName(realmUser.displayName());
            entity.email(realmUser.email());
            entity.keycloakUserId(realmUser.id());
            entity.active(realmUser.enabled());
        }
        byKeycloakId.put(realmUser.id(), entity);
    }

    private static Optional<UserJPA> findByUsername(EntityManager em, String username)
    {
        List<UserJPA> users = em.createQuery("SELECT u FROM UserJPA u WHERE u.username = :username", UserJPA.class)
            .setParameter("username", username)
            .setMaxResults(1)
            .getResultList();
        return users.isEmpty() ? Optional.empty() : Optional.of(users.getFirst());
    }

    private static Optional<UserJPA> findByKeycloakId(EntityManager em, String keycloakUserId)
    {
        List<UserJPA> users = em.createQuery("SELECT u FROM UserJPA u WHERE u.keycloakUserId = :keycloakUserId", UserJPA.class)
            .setParameter("keycloakUserId", keycloakUserId)
            .setMaxResults(1)
            .getResultList();
        return users.isEmpty() ? Optional.empty() : Optional.of(users.getFirst());
    }

    private static Map<String, UserJPA> findExistingByKeycloakId(EntityManager em)
    {
        List<UserJPA> users = em.createQuery(
                "SELECT u FROM UserJPA u WHERE u.keycloakUserId IS NOT NULL", UserJPA.class)
            .getResultList();
        Map<String, UserJPA> map = new HashMap<>();
        for (UserJPA user : users) {
            user.keycloakUserId().ifPresent(id -> map.put(id, user));
        }
        return map;
    }

    private static List<KeycloakUser> readUsers(String json)
    {
        try {
            List<Map<String, Object>> list = OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
            return list.stream().map(KeycloakUserSyncService::toKeycloakUser).toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Keycloak users payload", e);
        }
    }

    private static Map<String, Object> readObject(String json)
    {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Keycloak payload", e);
        }
    }

    private static KeycloakUser toKeycloakUser(Map<String, Object> json)
    {
        String id = requireNonBlank(asString(json.get("id")).orElse(null), "Keycloak user without id");
        String username = requireNonBlank(asString(json.get("username")).orElse(null), "Keycloak user without username");
        String first = asString(json.get("firstName")).orElse("");
        String last = asString(json.get("lastName")).orElse("");
        String displayName = (first + " " + last).trim();
        if (displayName.isBlank()) displayName = username;
        String email = asString(json.get("email")).orElse(id + "@keycloak.local");
        boolean enabled = asBoolean(json.get("enabled")).orElse(true);
        return new KeycloakUser(id, username, displayName, email, enabled);
    }

    private static Map<String, Object> userBody(String username, String displayName, String email, boolean enabled)
    {
        String trimmed = defaulted(displayName, username).trim();
        String firstName = trimmed;
        String lastName = "";
        int split = trimmed.indexOf(' ');
        if (split > 0) {
            firstName = trimmed.substring(0, split).trim();
            lastName = trimmed.substring(split + 1).trim();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("email", email);
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("enabled", enabled);
        map.put("emailVerified", true);
        return map;
    }

    private static String defaulted(@Nullable String value, String fallback)
    {
        if (value == null || value.isBlank()) return fallback;
        return value;
    }

    private static String requireNonBlank(@Nullable String value, String message)
    {
        if (value == null || value.isBlank()) throw new NotFoundException(message);
        return value;
    }

    private static Optional<String> asString(@Nullable Object value)
    {
        if (value instanceof String s && !s.isBlank()) return Optional.of(s);
        return Optional.empty();
    }

    private static Optional<Boolean> asBoolean(@Nullable Object value)
    {
        if (value instanceof Boolean b) return Optional.of(b);
        return Optional.empty();
    }

    private record KeycloakConfig(
        String serverUrl,
        String realm,
        String adminUsername,
        String adminPassword,
        String clientId
    )
    {
        private static KeycloakConfig read()
        {
            String serverUrl = ConfigProvider.getConfig()
                .getOptionalValue("pragma.keycloak.admin.server-url", String.class)
                .orElse("http://localhost:8080");
            String realm = ConfigProvider.getConfig()
                .getOptionalValue("pragma.keycloak.admin.realm", String.class)
                .orElse("pragma-realm");
            String adminUsername = ConfigProvider.getConfig()
                .getOptionalValue("pragma.keycloak.admin.username", String.class)
                .orElse("admin");
            String adminPassword = ConfigProvider.getConfig()
                .getOptionalValue("pragma.keycloak.admin.password", String.class)
                .orElse("admin");
            String clientId = ConfigProvider.getConfig()
                .getOptionalValue("pragma.keycloak.admin.client-id", String.class)
                .orElse("admin-cli");
            return new KeycloakConfig(trimTrailingSlash(serverUrl), realm, adminUsername, adminPassword, clientId);
        }

        private static String trimTrailingSlash(String value)
        {
            return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        }
    }

    private record KeycloakUser(
        String id,
        String username,
        String displayName,
        String email,
        boolean enabled
    )
    {
    }
}
