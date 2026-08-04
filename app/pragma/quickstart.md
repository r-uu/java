# pragma — Quickstart

## Voraussetzungen

| Tool           | Mindestversion | Hinweis                                |
|----------------|----------------|----------------------------------------|
| Java (GraalVM) | 25             | `java -version` muss GraalVM 25 zeigen |
| Maven          | 3.9+           | `mvn -version`                         |
| Docker         | beliebig       | für PostgreSQL-Container               |
| IntelliJ IDEA  | 2024.x+        | Community oder Ultimate                |

---

## Ersteinrichtung

### 1. Gesamtes Repo bauen (einmalig)

lib und pragma liegen im selben Mono-Repo. Ein Build vom Root installiert beides:

```bash
cd ~/develop/github/java
mvn install
```

Alternativ nur lib vorab installieren (wenn ausschließlich an pragma gearbeitet wird):

```bash
cd ~/develop/github/java
mvn install -pl lib -am
```

### 2. Nur pragma bauen

```bash
cd ~/develop/github/java
mvn install -pl app/pragma -am
```

Der Build kopiert dabei automatisch die Hibernate-JARs nach
`backend/rest/src/main/liberty/config/lib/global/` (Schritt `prepare-package`).

---

## Typischer Entwicklungs-Workflow

### Schritt 1 — PostgreSQL + Keycloak starten

```bash
docker compose up -d
```

Container:
- `pragma-postgres` (Host-Port 5432, DB/User/Passwort: `pragma`)
- `pragma-keycloak` (Port 8080, Admin `admin` / `admin`)

Falls `FATAL: password authentication failed for user "pragma"` auftritt, ist meist bereits ein
persistiertes Docker-Volume mit abweichenden PostgreSQL-Credentials vorhanden. Dann die DB
zurücksetzen und neu initialisieren:

```bash
docker compose down -v
docker compose up -d
```

Abweichende Liberty-Datasource-Credentials lassen sich über Liberty-Variablen wie
`-Ddatasource_database_password=...` überschreiben.

### Schritt 2 — Liberty-Server starten

**Produktion (einmaliger Start):**
```bash
mvn -pl backend/rest package liberty:run
```

**Entwicklung (Hot-Reload, empfohlen):**
```bash
mvn -pl backend/rest package liberty:dev
```

Wichtig: `liberty:dev` benötigt hier eine Maven-Version **>= 3.8.6**. Das in manchen
Distributionen per `apt` installierte Maven 3.6.x ist dafür zu alt.

Der Server ist erreichbar unter:
- `http://localhost:9090/pragma/api/task-groups`

Beim Start der JavaFX-App wird vor dem eigentlichen Fensterstart automatisch ein kurzer
Prueflauf ausgefuehrt. Wenn PostgreSQL, Keycloak oder das Backend nicht bereit sind,
bricht Pragma mit einer klaren Fehlermeldung ab.

### Schritt 3 — Datenbank befüllen

```bash
# Datenbank leeren + Testdaten anlegen (empfohlen für frischen Start)
mvn -pl frontend/rest-client exec:java -Dexec.mainClass=de.ruu.app.pragma.client.dbcommand.DBCommand

# Nur leeren
mvn -pl frontend/rest-client exec:java -Dexec.mainClass=de.ruu.app.pragma.client.dbcommand.DBClear

# Nur befüllen
mvn -pl frontend/rest-client exec:java -Dexec.mainClass=de.ruu.app.pragma.client.dbcommand.DBPopulate
```

`DBPopulate` legt 3 Gruppen mit 10 Tasks an (Vorgänger-, Teilaufgaben- und Gruppenrelationen).

### Schritt 4 — Integrationstests ausführen

Server muss laufen (Schritt 2).

```bash
mvn -pl frontend/rest-client verify
```

Tests mit `@DisabledOnServerNotListening` werden automatisch übersprungen,
wenn kein Server auf Port 9090 lauscht.

### Optional — Login-Abkürzung für Dev/Test

Der Login lässt sich über `pragma.auth.test-mode` steuern:

- `true` (Default): Dev/Test-Modus, Credentials werden automatisch aus den
  Defaults genutzt (`r-uu` / `r-uu`).
- `false`: bevorzugt dedizierte Admin-Credentials
  (`pragma.keycloak.admin.username` / `pragma.keycloak.admin.password`),
  alternativ `pragma.keycloak.username` / `pragma.keycloak.password`.

Für schnelles Umschalten ohne erneute Eingabe gibt es zusätzlich WSL-Aliases:

```bash
ruu-pragma-fx         # provisioniert r-uu + admin-keycloak und startet JavaFX mit r-uu / r-uu
ruu-pragma-fx-admin   # provisioniert r-uu + admin-keycloak und startet JavaFX mit admin-keycloak / r-uu
```

Alternativ frei per Property:

```bash
mvn -pl frontend/fx -am exec:java@pragma \
  -Dpragma.auth.test-mode=false \
  -Dpragma.keycloak.admin.username=<admin-user> \
  -Dpragma.keycloak.admin.password=<admin-password>
```

In IntelliJ kann dieselbe Steuerung über VM-Options gesetzt werden:

```text
-Dpragma.auth.test-mode=false -Dpragma.keycloak.admin.username=<admin-user> -Dpragma.keycloak.admin.password=<admin-password>
```

---

## IntelliJ Run Configurations

Alle Run Configs liegen in `.run/` und werden von IntelliJ automatisch erkannt.

| Config               | Typ         | Beschreibung                                     |
|----------------------|-------------|--------------------------------------------------|
| `pragma liberty:run` | Maven       | Server starten (Produktionsmodus)                |
| `pragma liberty:dev` | Maven       | Server starten (Hot-Reload für Entwicklung)      |
| `pragma db:clear`    | Application | Alle Daten in der DB löschen                     |
| `pragma db:populate` | Application | Testdaten anlegen (3 Gruppen, 10 Tasks)          |
| `pragma db:command`  | Application | DB leeren + Testdaten anlegen (clear + populate) |

Die Application-Configs benötigen keine VM-Parameter. Host und Port werden über MicroProfile Config
aus `META-INF/microprofile-config.properties` gelesen (Default: `localhost:9090`).
Zum Überschreiben: System-Property oder Umgebungsvariable setzen:

```
-Dpragma.rest-api.host=meinhost
-Dpragma.rest-api.port=9090
```

oder als Umgebungsvariablen `PRAGMA_REST_API_HOST` / `PRAGMA_REST_API_PORT`.

---

## DB-Command-Klassen

Die Klassen liegen in `frontend/rest-client`:
```
de.ruu.app.pragma.client.dbcommand
├── DBClear.java         — löscht alle Gruppen + Tasks (ruft DELETE /task-groups)
├── DBPopulate.java      — legt Testdaten an (Gruppen, Tasks, Relationen)
├── DBCommand.java       — führt DBClear + DBPopulate aus (hat eigene main())
├── DBClearRunner.java   — thin wrapper für IntelliJ-Config (→ DBClear.main)
└── DBPopulateRunner.java— thin wrapper für IntelliJ-Config (→ DBPopulate.main)
```

Alle Klassen lassen sich direkt per `main()` starten — kein CDI-Container nötig.
Die `@PostConstruct`- und `@PreDestroy`-Methoden der Clients werden explizit aufgerufen,
sodass der Jersey-Client korrekt initialisiert und am Ende geschlossen wird.

Host und Port lesen die Clients über MicroProfile Config (SmallRye Config als Implementierung):
- Default-Werte in `META-INF/microprofile-config.properties`
- Überschreibbar per System-Property (`-Dpragma.rest-api.host=...`) oder Umgebungsvariable

### Testdaten-Struktur (DBPopulate)

```
Gruppe "Analyse"
  A1: Anforderungen erfassen
  A2: Technologie-Stack festlegen  [Vorgänger: A1]
  A3: Architektur definieren       [Vorgänger: A2]

Gruppe "Entwicklung"
  D1: Backend implementieren       [Vorgänger: A3]
    D1a: REST-Endpoints            [Teilaufgabe von D1]
    D1b: JPA-Entities              [Teilaufgabe von D1]
  D2: Frontend implementieren      [Vorgänger: D1]

Gruppe "Test"
  T1: Integrationstests            [Vorgänger: D1]
  T2: Abnahmetests                 [Vorgänger: T1, D2]
```

---

## Nützliche Befehle

```bash
# Nur Backend-Module bauen (schneller)
mvn -pl core,bean,backend/dto,backend/jpa,backend/rest install

# Server-Logs live verfolgen (liberty:dev gibt Logs direkt aus)
# Im Terminal, das liberty:dev läuft, ist das automatisch sichtbar

# Docker-Container stoppen
docker compose down

# Docker-Volume löschen (DB zurücksetzen)
docker compose down -v

# Alle Targets bereinigen
mvn clean
```

---

## Ports-Übersicht

| Dienst                     | Port | Hinweis                        |
|----------------------------|------|--------------------------------|
| Liberty HTTP               | 9090 | REST-API unter `/pragma/api`   |
| Liberty HTTPS              | 9543 |                                |
| PostgreSQL                 | 5432 | Container `pragma-postgres`    |
