# Quickstart — lib-java

Allgemeine Java-Bibliothek als Maven Multi-Modul Projekt. Dieses Dokument beschreibt die Entwicklungsumgebung, Start der Infrastruktur-Services und die wichtigsten Build- und Test-Workflows.

---

## Inhaltsverzeichnis

1. [Voraussetzungen](#1-voraussetzungen)
2. [Infrastruktur starten](#2-infrastruktur-starten)
3. [Datenbank-Setup](#3-datenbank-setup)
4. [Credentials](#4-credentials)
5. [Maven-Workflows](#5-maven-workflows)
6. [IntelliJ Run Configurations](#6-intellij-run-configurations)
7. [Integrationstests — Überblick](#7-integrationstests--überblick)

---

## 1. Voraussetzungen

| Tool           | Mindestversion      | Anmerkung                |
|----------------|---------------------|--------------------------|
| Java           | 25 (GraalVM 25.0.1) | `java -version`          |
| Maven          | 3.9.9               | `mvn -version`           |
| Docker         | 28.x                | `docker --version`       |
| Docker Compose | v2.22+              | `docker compose version` |

Das Projekt läuft unter WSL2 (Linux). Maven-Surefire ist projektweit mit `useModulePath=false` konfiguriert — Tests laufen auf dem Classpath, auch wenn Module `module-info.java` besitzen (Hybrid-JPMS-Strategie).

---

## 2. Infrastruktur starten

Die Docker-Compose-Datei liegt unter:

```
/home/r-uu/config/docker/docker-compose.yml
```

```bash
cd /home/r-uu/config/docker
docker compose up -d
```

### Gestartete Services

| Container       | Image                              | Port     | Zweck                                                      |
|-----------------|------------------------------------|----------|------------------------------------------------------------|
| `postgres`      | `postgres:16-alpine`               | **5432** | PostgreSQL — alle Datenbanken (lib_test, pragma, keycloak) |
| `keycloak`      | `quay.io/keycloak/keycloak:latest` | **8080** | Keycloak IAM (Modus: start-dev)                            |
| `jasperreports` | `docker-jasperreports` (lokal)     | **8090** | JasperReports-Server                                       |

> **Hinweis:** Der `keycloak`-Container startet erst, wenn `postgres` healthy ist (`depends_on: condition: service_healthy`). Das Hochfahren kann 60–90 Sekunden dauern.

Status prüfen:
```bash
docker ps
```

---

## 3. Datenbank-Setup

Der laufende `postgres`-Container enthält folgende Datenbanken und Rollen:

| Datenbank   | Eigentümer  | Zweck                                  |
|-------------|-------------|----------------------------------------|
| `lib_test`  | `lib_test`  | Integrationstests (JPA, JDBC, Toolbox) |
| `pragma` | `pragma` | Anwendungsdaten (Haupt-App)            |
| `keycloak`  | `keycloak`  | Keycloak-Backend-DB                    |

Die Datenbanken und Rollen werden beim ersten Start des Containers durch Init-Skripte im Volume-Mount angelegt:

```
/home/r-uu/config/docker/database-init/
```

Manuell prüfen:
```bash
docker exec postgres psql -U postgres -c '\l'    # Datenbanken
docker exec postgres psql -U postgres -c '\du'   # Rollen/User
```

---

## 4. Credentials

### Datenbankzugänge

| Datenbank   | User        | Passwort              | Port |
|-------------|-------------|-----------------------|------|
| `lib_test`  | `lib_test`  | `lib_test`            | 5432 |
| `pragma` | `pragma` | (im DB-Init gesetzt)  | 5432 |
| `keycloak`  | `keycloak`  | (intern, via Compose) | 5432 |

### Keycloak Admin-Zugang

| Eigenschaft     | Wert                  |
|-----------------|-----------------------|
| URL             | http://localhost:8080 |
| Admin-User      | `admin`               |
| Admin-Passwort  | `admin`               |
| Realm für Tests | `pragma-realm`     |

### BOM-Defaults (Maven)

Das BOM (`bom/pom.xml`) definiert Standard-Credentials als Maven-Properties. Diese werden beim Build verwendet, wenn keine Umgebungsvariablen gesetzt sind:

```xml
<postgres_admin_username>postgres_admin_default</postgres_admin_username>
<postgres_admin_password>postgres_admin_default</postgres_admin_password>
<postgres_libtest_username>lib_test_default</postgres_libtest_username>
<postgres_libtest_password>lib_test_default</postgres_libtest_password>
<keycloak_admin_username>admin_default</keycloak_admin_username>
<keycloak_admin_password>admin_default</keycloak_admin_password>
<test_username>test_default</test_username>
<test_password>test_default</test_password>
```

**Für produktive/reale Werte:** Das Maven-Profil `load-env` im BOM liest eine `.env`-Datei im Projektverzeichnis via `properties-maven-plugin` ein. Dort können Credentials überschrieben werden:

```bash
# /home/r-uu/develop/github/lib-java/.env  (nicht eingecheckt)
postgres_libtest_username=lib_test
postgres_libtest_password=lib_test
```

Profil aktivieren:
```bash
mvn verify -Pload-env
```

### MicroProfile Config (Laufzeit)

Integrationstests lesen Credentials zur Laufzeit über MicroProfile Config (`microprofile-config.properties` in `src/test/resources/META-INF/`). Format:

```properties
database.host=${POSTGRES_LIB_TEST_HOST:localhost}
database.port=${POSTGRES_LIB_TEST_PORT:5432}
database.name=${POSTGRES_LIB_TEST_DATABASE:lib_test}
database.user=${POSTGRES_LIB_TEST_USER:lib_test}
database.password=${POSTGRES_LIB_TEST_PASSWORD:lib_test}
```

Umgebungsvariablen (Ordinal 300) überschreiben Datei-Werte (Ordinal 100). System Properties (Ordinal 400) überschreiben beides — die IntelliJ-Run-Configs nutzen das über `-D`-Argumente.

---

## 5. Maven-Workflows

### Vollständiger Build

```bash
cd /home/r-uu/develop/github/lib-java
mvn clean install
```

Baut alle Module, führt Unit-Tests aus. Integrationstests werden **nicht** ausgeführt (erfordern laufende Services und explizite Run Configs oder Failsafe).

### Einzelnes Modul bauen

```bash
mvn clean install -pl jpa/core -am
```

`-am` (also-make) baut alle Abhängigkeiten mit.

### Liberty Development Server starten

```bash
cd liberty/server
mvn liberty:dev
```

Hot-Reload-Server für die JAX-RS- und CDI-Komponenten in `ws/rs`. Der Server startet auf Port 9080 (`http://localhost:9080`). Änderungen werden automatisch deployt. Mit `q` + Enter beenden.

### Liberty Integrationstests ausführen (CI)

```bash
cd liberty/server
mvn verify
```

Lifecycle: `liberty:create` → `liberty:install-feature` → `liberty:start` → Failsafe IT-Tests → `liberty:stop`. Erfordert keinen manuell laufenden Server.

### BOM-only Update

```bash
mvn install -pl bom
```

Sinnvoll nach Änderungen an `bom/pom.xml`, damit andere Module die neue BOM-Version finden.

---

## 6. IntelliJ Run Configurations

Die Konfigurationen liegen in `.idea/runConfigurations/` und werden automatisch von IntelliJ geladen.

### Datenbank-Integrationstests

#### `IT - jdbc - postgres`
Testet die JDBC-Utilities gegen die `lib_test`-Datenbank.

| Eigenschaft     | Wert                       |
|-----------------|----------------------------|
| Modul           | `r-uu.lib.jdbc.postgres`   |
| Package         | `de.ruu.lib.jdbc.postgres` |
| DB-Port         | 5432                       |
| DB-Name         | `lib_test`                 |
| Voraussetzung   | `postgres`-Container läuft |

#### `IT - jpa - se - hibernate`
Testet JPA/Hibernate SE-Konfiguration.

| Eigenschaft   | Wert                              |
|---------------|-----------------------------------|
| Modul         | `r-uu.lib.jpa.se.hibernate`       |
| Package       | `de.ruu.lib.jpa.se.hibernate`     |
| DB-Port       | 5434 (via `-Ddatabase.port=5434`) |
| DB-Name       | `lib_test`                        |
| Voraussetzung | PostgreSQL auf Port 5434          |

> **Hinweis:** Diese Config erwartet einen separaten PostgreSQL-Container auf Port 5434. Falls nur der Haupt-Container auf 5432 läuft, muss der Port in der Run Config oder per Umgebungsvariable `POSTGRES_LIB_TEST_PORT=5432` angepasst werden.

#### `IT - jpa - se - hibernate - postgres - demo`
Demo-Anwendung für JPA-Entitäten mit Hibernate und PostgreSQL.

| Eigenschaft   | Wert                                        |
|---------------|---------------------------------------------|
| Modul         | `r-uu.lib.jpa.se.hibernate.postgres.demo`   |
| Package       | `de.ruu.lib.jpa.se.hibernate.postgres.demo` |
| DB-Port       | 5432                                        |
| DB-Name       | `lib_test`                                  |
| Voraussetzung | `postgres`-Container läuft                  |

Die Persistence Unit heißt `lib_test` und wird über System Properties an den `AbstractEntityManagerProducer` übergeben.

#### `IT - postgres - toolbox`
Tests für PostgreSQL-Toolbox-Utilities (Verbindungstest, Schema-Utilities etc.).

| Eigenschaft   | Wert                                                                   |
|---------------|------------------------------------------------------------------------|
| Modul         | `r-uu.lib.postgres.toolbox`                                            |
| Package       | `de.ruu.lib.postgres`                                                  |
| Voraussetzung | `postgres`-Container läuft (liest Credentials aus MicroProfile Config) |

#### `IT - all - db`
Compound-Config, die alle vier DB-Integrationstests gleichzeitig startet.

---

### Liberty Run Configurations

#### `Liberty - dev`
Startet den Open Liberty Server im Entwicklungsmodus.

| Eigenschaft        | Wert                           |
|--------------------|--------------------------------|
| Typ                | Maven                          |
| Goal               | `liberty:dev`                  |
| Arbeitsverzeichnis | `$PROJECT_DIR$/liberty/server` |
| Server-Port        | 9080                           |

#### `Liberty - IT (verify)`
Startet Liberty, führt alle `@IT`-Tests aus und stoppt Liberty wieder.

| Eigenschaft        | Wert                           |
|--------------------|--------------------------------|
| Typ                | Maven                          |
| Goal               | `verify`                       |
| Arbeitsverzeichnis | `$PROJECT_DIR$/liberty/server` |
| Server-Port        | 9080                           |

---

## 7. Integrationstests — Überblick

### Automatische Test-Unterdrückung: `@DisabledOnServerNotListening`

Viele Integrationstests sind mit der Custom-JUnit-5-Extension `@DisabledOnServerNotListening` annotiert. Sie prüft vor dem Test-Start per TCP-Connect, ob der konfigurierte Host/Port erreichbar ist. Ist kein Server verfügbar, wird der Test mit `disabled` markiert (kein Fehler). Das ermöglicht normales `mvn install` ohne laufende Infrastruktur.

### Keycloak-Tests

Keycloak-Integrationstests sind mit `@Disabled` annotiert und müssen manuell aktiviert werden. Voraussetzungen:
- Keycloak läuft auf `localhost:8080`
- Realm `pragma-realm` ist angelegt
- Admin-Credentials: `admin` / `admin`

### JasperReports-Tests

JasperReports-Tests erwarten einen Server auf `localhost:8090`. Der Container `jasperreports` läuft standardmäßig.

### Port-Übersicht

| Service                                    | Port | Container               |
|--------------------------------------------|------|-------------------------|
| PostgreSQL (Hauptinstanz)                  | 5432 | `postgres`              |
| PostgreSQL (lib_test, separater Container) | 5434 | ggf. manuell starten    |
| Keycloak                                   | 8080 | `keycloak`              |
| JasperReports                              | 8090 | `jasperreports`         |
| Open Liberty                               | 9080 | lokal via `liberty:dev` |
