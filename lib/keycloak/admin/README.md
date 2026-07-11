# Keycloak Configuration Validator

Utilities für die Validierung der Keycloak-Konfiguration und JWT-Token-Analyse.

## Überblick

Dieses Modul enthält wiederverwendbare Komponenten zur Validierung der Konsistenz zwischen:
- Keycloak-Konfiguration (Rollen, Audiences, Token-Lifetimes)
- Backend REST APIs (@RolesAllowed Annotationen)
- JWT-Token-Claims von Client-Anfragen

---

## ⏱️ Token-Lebensdauern (Keycloak Realm Setup)

Das `KeycloakRealmSetup` konfiguriert automatisch optimale Token-Lebensdauern:

| Token-Typ | Lebensdauer | Zweck |
|-----------|-------------|-------|
| **Access Token** | 30 Minuten | Verhindert häufige Re-Authentifizierung |
| **Refresh Token** | 30 Minuten (Idle) | Automatische Verlängerung bei Aktivität |
| **SSO Session Max** | 10 Stunden | Maximum Session-Lebensdauer |
| **Offline Session** | 30 Tage | "Remember Me" Funktionalität |

**Warum diese Werte?**
- ✅ **30 Min Access Token:** Verhindert "Session Expired"-Fehler direkt nach Login
- ✅ **30 Min Idle Timeout:** Token wird automatisch erneuert bei aktiver Nutzung  
- ✅ **10 Std Maximum:** Zwingt zu Re-Login nach einem Arbeitstag
- ✅ **Balance:** Sicherheit vs. Benutzerfreundlichkeit

**Problem gelöst:** Früher bekamen User direkt nach Login "Session Expired"-Dialoge, weil Default-Token-Lebensdauer zu kurz war (oft nur 1-5 Minuten).

---

## Komponenten

### KeycloakConfigValidator

Zentrale Validator-Klasse mit statischen Methoden für verschiedene Validierungsszenarien:

```java
// Rollen-Validierung
Set<String> tokenRoles = Set.of("task-read", "task-write");
Set<String> requiredRoles = Set.of("task-read", "task-write", "task-delete");
RoleValidationResult result = KeycloakConfigValidator.validateRoles(tokenRoles, requiredRoles);

if (!result.isValid()) {
    System.out.println(result.getSummary());
    result.getRecommendations().forEach(System.out::println);
}

// Audience-Validierung
Set<String> tokenAudiences = Set.of("pragma-backend", "account");
AudienceValidationResult audResult = KeycloakConfigValidator.validateAudience(
    tokenAudiences, "pragma-backend");

// Naming-Consistency Check
Set<String> roles = Set.of("task-read", "taskgroup-read"); // Inkonsistent!
NamingConsistencyResult namingResult = 
    KeycloakConfigValidator.validateRoleNamingConsistency(roles);

// Token-Lifetime Validierung
TokenLifetimeValidationResult lifetimeResult = 
    KeycloakConfigValidator.validateTokenLifetime(300, 1800);
```

### JwtTokenParser

Lightweight JWT-Parser für Debugging und Validierung (ohne externe Dependencies):

```java
String token = "eyJhbGc..."; // JWT Token
TokenInfo tokenInfo = JwtTokenParser.parseToken(token);

System.out.println("Issuer: " + tokenInfo.getIssuer());
System.out.println("Subject: " + tokenInfo.getSubject());
System.out.println("Audiences: " + tokenInfo.getAudiences());
System.out.println("Roles: " + tokenInfo.getRoles());
System.out.println("Expired: " + tokenInfo.isExpired());
System.out.println("Remaining: " + tokenInfo.getRemainingLifetimeSeconds() + "s");
```

**Hinweis:** Dieser Parser ist nur für Debugging gedacht. Für Produktiv-Validierung sollten robuste JWT-Bibliotheken wie jose4j oder nimbus-jose-jwt verwendet werden.

### ValidationReport

Kombiniert alle Validierungen in einem umfassenden Report:

```java
ValidationReport report = ValidationReport.builder()
    .roleValidation(roleResult)
    .audienceValidation(audResult)
    .namingConsistency(namingResult)
    .tokenLifetime(lifetimeResult)
    .build();

if (!report.isFullyValid()) {
    System.out.println(report.getDetailedReport());
}
```

## Integration im pragma-Backend

Das Backend-Modul `app/pragma/backend/api/ws_rs` verwendet diese Utilities für:

### 1. Startup-Validierung

`KeycloakConfigurationValidator` scannt beim Start alle REST-Services und validiert:
- Alle @RolesAllowed Annotationen werden extrahiert
- Naming-Konsistenz wird geprüft
- Setup-Anleitungen werden geloggt

### 2. Admin-Endpoint

`KeycloakValidationEndpoint` bietet REST-Endpoints für Runtime-Validierung:

```bash
# Konfiguration neu validieren
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:9080/pragma/admin/keycloak/validate-config

# Konkreten Token validieren
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"token": "eyJhbGc..."}' \
  http://localhost:9080/pragma/admin/keycloak/validate-token
```

**Sicherheit:** Diese Endpoints erfordern die Rolle `task-group-admin`.

## Häufige Probleme und Lösungen

### Problem: "Missing required role(s)"

**Ursache:** Token enthält nicht alle Rollen, die Backend erwartet.

**Lösung:**
```bash
# Rolle in Keycloak erstellen
docker exec keycloak /opt/keycloak/bin/kcadm.sh create roles \
  -r pragma-realm -s name=task-read

# Rolle dem User zuweisen
docker exec keycloak /opt/keycloak/bin/kcadm.sh add-roles \
  -r pragma-realm --uusername r-uu --rolename task-read
```

### Problem: "Token audience does not contain expected value"

**Ursache:** Keycloak-Client hat keinen Audience-Mapper konfiguriert.

**Lösung:**
1. Keycloak Admin Console → Clients → pragma-frontend
2. Client scopes → pragma-frontend-dedicated → Add mapper
3. By configuration → Audience
4. Included Custom Audience = `pragma-backend`

### Problem: "Naming inconsistencies detected"

**Ursache:** Gemischte Namenskonventionen (z.B. `taskgroup-read` vs. `task-group-read`).

**Lösung:** Standardisierung auf eine Konvention:
- ✓ Empfohlen: `task-group-read`, `task-group-write` (mit Bindestrichen)
- ✗ Vermeiden: Gemischte Stile im selben Projekt

### Problem: "Token lifetime too short"

**Ursache:** Access Token Lifetime < 60 Sekunden.

**Lösung:**
1. Keycloak Admin Console → Realm Settings → Tokens
2. Access Token Lifespan = `5 minutes` (empfohlen)
3. Refresh Token Lifespan = `30 minutes`

## Testing

Umfassende Unit-Tests sind vorhanden:

```bash
mvn test -pl lib/keycloak_admin
```

## Dependencies

Minimale Dependencies für maximale Wiederverwendbarkeit:
- SLF4J (Logging)
- Lombok (Boilerplate-Reduktion)
- JUnit 5 (Tests)

Keine externen JWT-Bibliotheken → Einfacher Parser für Debugging-Zwecke.

## Lizenz

Internes r-uu Projekt
