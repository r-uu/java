# Test Scenario Spec: pragma baseline complete

Dieses Dokument beschreibt die fachliche und technische Spezifikation für eine ausführbare Setup-Klasse `SetupPragmaTestScenarioBaseline`.

## Ziel

Die Klasse erzeugt eine reproduzierbare Baseline-Testumgebung (`pragma test scenario baseline complete`) und stellt sicher, dass die Datenstände von Pragma-Postgres und Keycloak zueinander konsistent sind.

## Scope

1. Backup der aktuellen Pragma-Daten (Postgres + Keycloak)
2. Löschen der bestehenden Pragma-Daten
3. Erzeugen eines definierten Baseline-Szenarios
4. Verifikation des Ergebnisses

## Nicht-Ziele

1. Produktive Migrationen
2. Teil-Updates einzelner Datensätze
3. Fachlich vollständige Langzeitplanung über das Baseline-Szenario hinaus

## Ausführung und Konfiguration

`SetupPragmaTestScenarioBaseline` ist als lokal ausführbare Klasse verfügbar (z. B. per `main` oder Runner).

Konfigurierbare Parameter:

| Parameter | Bedeutung | Default |
|---|---|---|
| `pragma.backup.dir` | Zielverzeichnis für Backups | `app/pragma/dbbackup` |
| `pragma.scenario.name` | Name des Szenarios | `pragma test scenario baseline complete` |
| `pragma.scenario.description` | Beschreibung des Szenarios | `test scenario for pragma` |
| `pragma.scenario.seed` | Seed für deterministische Datenerzeugung | `20260805` |
| `pragma.setup.force` | Setup trotz vorhandener Daten erzwingen | `false` |

## Konsistenzanforderung Postgres ↔ Keycloak

Die Sicherung von Postgres und Keycloak muss als gemeinsamer, konsistenter Snapshot behandelt werden.

Mindestanforderungen:

1. Kein fachlicher Schreibverkehr während des Snapshot-Fensters (Wartungsmodus oder äquivalente Sperre).
2. Beide Backups erhalten dieselbe `snapshot_id`.
3. Nach dem Setup referenzieren alle Pragma-Benutzer genau einen gültigen Keycloak-User und umgekehrt, soweit fachlich vorgesehen.

## Backup

### Dateibenennung

Backups liegen unter `app/pragma/dbbackup` mit Zeitstempel im Format `yyyy-MM-dd_HH-mm-ss`:

1. `backup_pragma_postgres_<timestamp>.dump`
2. `backup_pragma_keycloak_<timestamp>.dump`
3. `backup_pragma_manifest_<timestamp>.json` (enthält `snapshot_id`, Dateinamen, Prüfsummen, Start-/Endzeit)

### Postgres

Pragma-Postgres wird über `PostgresToolBox` (lib) gesichert.

### Keycloak

Keycloak wird über ein dediziertes Export-Verfahren gesichert.

Offener technischer Entscheid:

1. Keycloak-Export direkt über Keycloak-Tooling, oder
2. Keycloak auf externe Postgres-DB + Backup über `PostgresToolBox`.

Bis zur Entscheidung gilt: Setup ist nur freigegeben, wenn ein reproduzierbarer Restore-Nachweis für den gewählten Weg vorliegt.

## Ablauf (Soll-Verhalten)

1. Preconditions prüfen (Erreichbarkeit DB/Keycloak, Schreibsperre aktivierbar, Backup-Verzeichnis beschreibbar).
2. Gemeinsamen Snapshot erstellen (Postgres + Keycloak + Manifest).
3. Bestehende Pragma-Daten löschen (Postgres + Keycloak).
4. Baseline-Daten erzeugen.
5. Konsistenz- und Akzeptanzprüfung ausführen.
6. Schreibsperre aufheben.

## Fehlerbehandlung und Rollback

Bei Fehlern gilt fail-fast mit eindeutiger Fehlermeldung und Exit-Code ungleich 0.

Rollback-Regeln:

1. Fehler vor Löschung: Abbruch ohne Datenänderung.
2. Fehler nach Löschung und vor vollständigem Setup: automatischer Restore des erzeugten Snapshots.
3. Fehler im Restore: Prozess endet in definiertem Fehlerzustand; manueller Eingriff notwendig; zuletzt erfolgreicher Schritt wird protokolliert.

Keine stillen Fehler, kein „best effort“-Weiterlaufen.

## Fachliche Baseline-Daten

### Szenario-Metadaten

| Feld | Wert |
|---|---|
| name | `pragma test scenario baseline complete` |
| description | `test scenario for pragma` |
| created_at | Laufzeit-Zeitpunkt |
| updated_at | Laufzeit-Zeitpunkt |

### Zeitraum

Alle erzeugten Tasks liegen im Zeitraum `01.01.2026` bis `31.03.2026`.

### Teams

Folgende Teams werden erzeugt:

1. Team Analyse
2. Team Architecture and Design
3. Team Development and Deployment
4. Team Quality Assurance

Rahmenbedingungen:

1. Je Team 3 Mitglieder
2. Zielauslastung je Mitglied: 70% von 40h/Woche
3. Scrum-Organisation mit 2-Wochen-Sprints

### Rollen und Verantwortungen

#### Team Analyse

Verantwortlich für Requirements Engineering, fachliche Spezifikationen je Phase und laufende Klärung fachlicher Fragen.
Ein Product Owner priorisiert Anforderungen und pflegt das Product Backlog.

#### Team Architecture and Design

Verantwortlich für technische Grundsatzentscheidungen, Architektur und Lösungsdesign auf Basis der Analyse-Spezifikationen.

#### Team Development and Deployment

Verantwortlich für Implementierung, Integration, Deployment und Wartung.

#### Team Quality Assurance

Verantwortlich für Testplanung, Testdurchführung und Qualitätsnachweise.

### Features und Tasks

`SetupPragmaTestScenarioBaseline` erzeugt generische Features und ordnet Tasks den Teams und Teammitgliedern zu.
Die Task-Planung enthält bewusst zeitliche Überlappungen, damit Überlastsituationen entstehen.
Diese Überlastsituationen müssen in Pragma sichtbar markiert werden.

## Akzeptanzkriterien

Der Setup-Lauf ist erfolgreich, wenn alle Punkte erfüllt sind:

1. Für Postgres und Keycloak existiert je ein Backup mit identischer `snapshot_id`.
2. Das Manifest existiert und enthält Dateinamen + Prüfsummen.
3. Das Szenario `pragma test scenario baseline complete` ist exakt einmal vorhanden.
4. Alle vier Teams existieren und haben jeweils genau drei Mitglieder.
5. Alle Baseline-Tasks liegen vollständig im Zeitraum `01.01.2026` bis `31.03.2026`.
6. Es existieren mindestens zwei geplante Überlastsituationen, die in Pragma als Überlast markiert sind.
7. Postgres- und Keycloak-Referenzen sind konsistent (keine verwaisten Benutzerreferenzen).

## Minimales Logging

Pro Lauf werden mindestens protokolliert:

1. `snapshot_id`
2. Start-/Endzeit und Dauer
3. Anzahl erzeugter Teams, Mitglieder, Features, Tasks
4. Anzahl erkannter Überlastsituationen
5. Erfolg/Fehlschlag inkl. Fehlerursache