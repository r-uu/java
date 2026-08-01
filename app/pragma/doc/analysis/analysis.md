# Projekt **pragma**

## Hinweise für KI-Agenten

Diese Datei dient als Kontext-Datei für AI-Agenten wie Claude Code oder Gemini. Sie soll bei jedem Chat mit AI-Agenten berücksichtigt und automatisch aktuell gehalten werden. Sie beschreibt vor allem die fachlichen Aspekte des Projekts. Weitere Aspekte werden in anderen Kontext-Dateien beschrieben, die ebenso berücksichtigt und behandelt werden sollen. Eine Übersicht der Kontext-Dateien findet sich in der [übergeordneten Kontext-Datei](https://github.com/r-uu/java/blob/main/context.md).

## Projektname

**pragma** — aus dem Griechischen, „die erledigte Tat". Der Wortstamm *prag-* (von *prattein* = tun, planen, handeln) umfasst den ganzen Zyklus: planen → handeln → fertigstellen.

- Slogan-Kandidaten: *plan – execute – deliver* / *plan – perform – impact*

## Fachliche Projektziele

Das zu entwickelnde Softwaresystem pragma soll dem Anwender dabei helfen, in einem Projekt anfallenden Aufgaben (Task) zu strukturieren und den zeitlichen Ablauf für die Umsetzung der Aufgaben zu planen. Task können dabei in Taskgruppen (TaskGroup) organisiert werden.

Es orientiert sich inhaltlich an den jeeeraaah-Projekten [backend](https://github.com/r-uu/main_java) und [frontend](https://github.com/r-uu/main_cmp), stellt aber einen echten Neuanfang dar.

### Strukturierung von Tasks

Im Projekt geht es um

- das Erreichen von Zielen und
- die Planung und Durchführung von dazu zu erledigenden Aufgaben.

Aufgaben können dazu nach unterschiedlichen Kategorien organisiert werden:

- [Task-Gruppen](#taskgruppen),
- [Aggregationen von Tasks](#aggregation-von-tasks) und
- [Abfolgen von Tasks](#abfolge-von-tasks).

#### Taskgruppen

Tasks können in Gruppen organisiert werden.

#### Aggregation von Tasks

Die Strukturierung von Aufgaben soll es ermöglichen, komplexe Aufgaben in kleinere Teilaufgaben zu gliedern. Dieser Strukturierungsschritt soll sich beliebig häufig wiederholen lassen. Dabei entsteht eine Hierarchie von Aufgaben und Teilaufgaben. Diese Hierarchie ist eine Aggregatsbeziehung, bei der die Teile unabhängig von anderen Teilen existieren können.

#### Abfolge von Tasks

Tasks können in Vorgänger-/Nachfolgerbeziehungen (predecessor-/successor-Beziehungen) gesetzt werden.

Theoretisch sollte eine "physische" Relation zwischen Task-Objekten dazu ausreichend sein.

#### Regeln für die Strukturierung von Tasks

- Technisch gesehen handelt es sich bei den genannten Relationen jeweils um Aggregatsbeziehungen. D. h. Tasks existieren im Unterschied zur Komposition unabhängig von ihren Beziehungen.
- Fachlich gesehen sind bestimmte Konstellationen verboten: weder in der Aggregations- noch in der Abfolge-Beziehung dürfen aus fachlicher Sicht Zirkel entstehen. In der Praxis kann das aber passieren, wenn dem Anwender der Überblick über bestehenden Beziehungen verloren geht. Das System lässt solche Beziehungen zu, bietet aber optional Funktionalität um solche Zirkel aufzulösen.

## UI

Die pragma UI enthält mehrere Tabs. Auf jedem Tab soll die Strukturierung von Tasks in einer anderen Form dargestellt werden.

Am oberen linken Rand der Tabs befindet sich eine Dropdown-Box, die die verschiedenen Taskgruppen (TaskGroup) des Systems anzeigt. Die ausgewählte Taskgruppe wird in der UI dargestellt. Zusätzlich befindet sich dort ein Button "New TaskGroup".

Am oberen rechten Rand der Tabs befinden sich Buttons "Backup" und "Restore". Über diese lässt sich der aktuelle Zustand des Systems sichern und wiederherstellen. PostgresUtil stellt die Funktionalität zur Verfügung.

### Hierarchies Tab

- Die Sortierung der Tasks in der super-/sub-Hierarchie sollte bequem konfigurierbar sein (z. B. nach Alphabet, nach Start- oder Enddatum, nach Fälligkeitsdatum, etc.).
- Eine nützliche und mächtige Filterfunktion sollte es ermöglichen, die Darstellung in der super-/sub-Hierarchie auf bestimmte Tasks zu beschränken (z. B. nur Tasks mit bestimmten Status, nur Tasks mit bestimmten Tags, etc.).
- Predecessor-/Successor-Beziehungen sollen entsprechend der jeweiligen Beziehung in einer Baumstruktur dargestellt werden. Gibt es mehrere Beziehungen auf einer Ebene, sollen diese alphabetisch sortiert werden. Evtl. sollen die Beziehungen auch nach anderen Kriterien sortierbar sein (z. B. nach Start- oder Enddatum, nach Fälligkeitsdatum, etc.).

### Gantt Tab

### Graph Tab

Der Graph Tab verhält sich so, als habe er ein unsichtbares, gleich- bzw. regelmäßiges Raster, an dem sich die Positionierung der dargestellten Objekte orientiert.

Im Graph Tab werden alle Tasks der ausgewählten Taskgruppe initial so angeordnet, dass die Root-Tasks (also die ohne Parent-Task) untereinander (Sortierung alphabetisch) am linken Rand des Tabs erscheinen. Die direkten Nachfolger der Root-Tasks werden rechts neben den zugehörigen Root-Tasks angeordnet. Gibt es mehrere Nachfolger-Tasks, werden die untereinander positioniert (sortierung alphabetisch). Diese Art der Anordnung von Nachfolgern wiederholt sich rekursiv für alle Tasks.

Rekursiver Baum-Layout-Algorithmus:
- Jeder Root-Task beginnt auf derselben Zeile wie sein erstes Kind (Sub-Task oder Nachfolger)
- Kinder folgen in der nächsten Spalte rechts

Die Nachfolger eines Tasks sind durch einen gerichteten, nach Möglichkeit orthogonalen Verbinder mit ihren Vorgänger-Tasks verbunden. Es können auch mehrere Vorgänger- und Nachfolger-Tasks existieren. Außerdem sind parent- und child-Beziehungen durch orthogonale Verbinder dargestellt.

Die Tasks lassen sich per Drag-and-Drop entlang des Rasters verschieben.

In dem tab kann per Mouse-Rad und Ctrl-+/Ctrl-- die Größe der Darstellung verändert werden (zoom in/out).

## Feature-Analyse

### Allgemeine Strategie für Änderungstracking

Zuletzt aktualisiert: 2026-07-27

#### Ausgangslage

Mehrere fachlich relevante Felder (u. a. Planung, Estimates, Work) können geändert werden.
Aktuell stehen diese Änderungen meist nur als neuer Ist-Zustand zur Verfügung; Historie und
Begründungen sind nicht systematisch vorhanden.

Gleichzeitig soll das Produkt in der frühen Phase nicht durch zu viel Prozess- und UI-Komplexität
überfrachtet werden.

#### Kernfrage

Wie gehen wir **allgemein** mit Änderungen um:
- an welchen Stellen tracken,
- in welcher Tiefe tracken,
- ab wann sichtbar in der UI?

#### Strategische Optionen

##### Option A: Kein Tracking (nur aktueller Zustand)

**Vorteile**
- Minimaler Aufwand.
- Maximale UI-Einfachheit.

**Nachteile**
- Kaum Auswertbarkeit.
- Keine belastbare Nachvollziehbarkeit.

##### Option B: Generisches Basis-Tracking + schrittweise Sichtbarkeit

**Vorteile**
- Gute Balance aus Zukunftssicherheit und schlanker UX.
- Neue Felder können später ohne Architekturbruch ergänzt werden.

**Nachteile**
- Etwas mehr technische Komplexität im Kernmodell.

##### Option C: Vollständiges Tracking-Produkt sofort

**Vorteile**
- Hohe Transparenz und Reporting-Fähigkeit ab Tag 1.

**Nachteile**
- Hohes Überfrachtungsrisiko (UI, Regeln, Pflegeaufwand).
- Verlangsamt frühe Produktiteration.

#### Empfehlung

**Option B**: technische Grundlage früh, Produktkomplexität spät.

1. Jetzt ein generisches Änderungsmodell einführen (Back-end-seitig).
2. Zunächst nur für wenige, fachlich kritische Felder aktivieren.
3. Historien-UI, Kennzahlen und strenge Pflichtregeln erst später ausrollen.

So bleibt die Anwendung leichtgewichtig und ausbaufähig.

#### Priorisierung: Wo zuerst tracken?

**Stufe 1 (früh):** Felder mit hoher Planungs-/Steuerungsrelevanz  
z. B. geplante Termine, zentrale Estimate-/Work-Felder, Status/Priority.

**Stufe 2 (später):** Felder mit mittlerer Relevanz  
z. B. Beschreibung, organisatorische Metadaten.

**Stufe 3 (optional):** Vollständige Historie aller Felder  
nur bei echtem Bedarf (Audit/Compliance/Reporting).

#### Minimaler Tracking-Datensatz (generisch)

Pro Änderung speichern:
- `entityType`, `entityId`
- `fieldName`
- `oldValue`, `newValue`
- `changedAt`
- `changedBy` / `source` (falls verfügbar)
- `reason` (optional oder regelbasiert verpflichtend)

#### Leitplanken gegen Überfrachtung

- Keine Historien-UI im MVP erzwingen.
- Gründe nur dort verpflichtend machen, wo fachlich klar sinnvoll.
- Nur echte Änderungen speichern (`oldValue != newValue`).
- Regeln inkrementell je Feldgruppe aktivieren, nicht global auf einmal.

#### Entscheidungshilfe: jetzt oder später?

- **Komplett später** ist möglich, aber teuer:
  - Keine Alt-Historie.
  - Nachträgliche Rekonstruktion kaum belastbar.
  - Höherer Migrationsaufwand.
- **Minimal jetzt** reduziert spätere Risiken bei niedriger Produktlast.

Daher: **Tracking-Backbone jetzt, UI-/Prozessstrenge schrittweise nach Bedarf**.

#### Offene Entscheidungen

1. Welche Felder gehören verbindlich in Stufe 1?
   - id, name, parentId
   - scheduledStart, scheduledFinish
   - workEstimateInitial, workEstimateCurrent, workActual
   - status, priority
2. Wann wird ein Änderungsgrund Pflicht (immer, je Feld, je Schwellwert)?
   - erstmal keine Pflicht
3. Reicht Freitext oder braucht es zusätzlich Kategorien?
   - erstmal nur Freitext
4. Welche Auswertungen sollen zuerst sichtbar werden (und für wen)?
   - scheduled Felder, work Felder, status, priority

### User, Groups, Member und Task-Zuständigkeit

Zuletzt aktualisiert: 2026-07-27

#### Zielbild

Das Modell soll drei Dinge gleichzeitig leisten:
- Anmeldung/Identität (`User`).
- Kollaborative Struktur (`Group`, `Member`).
- Flexible Zuordnung auf Tasks (User **und/oder** Group als Verantwortliche/Zuständige).

Dabei soll das System im MVP schlank bleiben und später ohne Bruch ausbaubar sein.

#### Domänenmodell (Vorschlag)

##### 1) Identität und Anmeldung

- `User`
  - Fachliche Person im System (`id`, `username`, `displayName`, `email`, `active`).
- `AuthAccount`
  - Technische Anmeldeinformation (z. B. Passwort-Hash, Login-Status, letzte Anmeldung).
  - 1:1 zu `User`, bewusst getrennt von Fachattributen.

**Nutzen der Trennung:** Auth kann später gegen externes IAM (Keycloak/OIDC) getauscht werden, ohne das Fachmodell `User` umzubauen.

##### 2) Gruppen und Mitgliedschaften

- `Group`
  - Team/Einheit (`id`, `name`, `description`, `active`).
- `Membership`
  - Verknüpfung `User ↔ Group` (n:m) mit Zusatzdaten:
  - `roleInGroup` (z. B. OWNER, COORDINATOR, MEMBER)
  - `validFrom`, `validTo` (optional)
  - `active`

**Warum eigene `Membership`-Entität:** zukünftige Anforderungen (Rollen, Gültigkeit, Historie) passen sauber hinein, ohne Sonderlogik in Join-Tabellen.

##### 3) Einheitliche Zuweisung auf Tasks

- `Assignable` (abstraktes Konzept / Interface)
  - Implementiert von `User` und `Group`.
- `TaskAssignment`
  - Verknüpfung `Task ↔ Assignable` (n:m) mit:
  - `assignmentType` (mind. `RESPONSIBLE`, `ASSIGNEE`)
  - `share` oder `priority` (optional für Reihenfolge/Gewichtung)
  - `validFrom`, `validTo` (optional)
  - `note` (optional)

**Interpretation:**
- **RESPONSIBLE** = verantwortet Ergebnis/Freigabe.
- **ASSIGNEE** = operativ zuständig für Umsetzung.

Damit sind folgende Fälle ohne Modellbruch möglich:
- genau ein verantwortlicher User,
- verantwortliche Group + zuständiger User,
- mehrere zuständige User/Groups.

##### 4) Verfügbarkeit und Kapazität je User (Tag + Zeitraum)

- `UserAvailability`
  - Verfügbarkeit eines Users für einen Zeitraum:
  - `userId`
  - `fromDate`, `toDate`
  - `capacityHoursPerDay` (z. B. 0, 4, 8)
  - `availabilityType` (z. B. AVAILABLE, LIMITED, ABSENT)
  - `note` (z. B. Urlaub, Teilzeit, Schulung)

- `UserAvailabilityRule` (optional für wiederkehrende Muster)
  - Wiederkehrende Regeln (z. B. „jeden Freitag 4h“, „Mo-Do 8h“).
  - Konkrete Tageswerte können aus Regeln berechnet und durch `UserAvailability` übersteuert werden.

**Prinzip:** Regeln liefern Default-Werte, explizite Einträge für konkrete Zeiträume/Tage haben Vorrang.

#### Komfort- und Flexibilitätsregeln

1. Kein Sonderfall für User vs. Group in der Task-Logik (immer über `Assignable`).
2. `TaskAssignment` bleibt die einzige Quelle für Zuständigkeiten.
3. UI zeigt standardmäßig nur aktive Zuordnungen; Historie bleibt optional einblendbar.
4. Für MVP möglichst wenige Pflichtfelder (keine frühe Prozessüberfrachtung).

#### MVP-Schnitt (schlank starten)

- `User`, `Group`, `Membership`, `TaskAssignment` einführen.
- `assignmentType` zunächst auf `RESPONSIBLE` und `ASSIGNEE` begrenzen.
- Keine komplexe Rechte-Matrix im ersten Schritt.
- Login-Basis (Session/JWT) bereitstellen, aber Berechtigungsmodell zunächst einfach halten.
- `UserAvailability` für konkrete Zeiträume einführen (ohne komplexe Regel-Engine).
- Kapazität im MVP als `capacityHoursPerDay` modellieren.

#### Ausbaupfad (später)

- Erweiterte Assignment-Typen (z. B. REVIEWER, INFORMED).
- Stellvertretung/Delegation.
- Wiederkehrende Verfügbarkeitsregeln (`UserAvailabilityRule`) und Ausnahmekalender.
- Aggregation User → Group-Verfügbarkeit (teambezogene Kapazität).
- Regelbasierte Validierung (z. B. pro Task genau 1 RESPONSIBLE).
- Auswertungen: Lastverteilung pro User/Group, Ownership-Lücken, Bottlenecks.
- Kapazitäts-/Auslastungsansichten pro Tag/Woche/Zeitraum.

#### Verfügbarkeitslogik (leichtgewichtig, aber belastbar)

1. Tageskapazität eines Users wird aus `UserAvailability` im angefragten Zeitraum bestimmt.
2. Fehlt ein Eintrag, gilt ein System-Default (z. B. 8h/Tag) oder die aktive Regel.
3. `ABSENT` erzwingt 0h, unabhängig von Default/Regel.
4. Spätere Auslastungslogik nutzt die Summe geplanter Task-Aufwände gegen verfügbare Stunden.

#### Beispiel (konzeptionell)

Task `T-4711`:
- `RESPONSIBLE` → Group „Backend Team“
- `ASSIGNEE` → User „anna“
- `ASSIGNEE` → User „sam“

Ergebnis: Verantwortung ist teambezogen stabil, operative Zuständigkeit bleibt flexibel bei Personalwechsel.

Verfügbarkeit „anna“:
- 2026-08-01 bis 2026-08-31: `capacityHoursPerDay = 6`
- 2026-08-12 bis 2026-08-16: `availabilityType = ABSENT` (Urlaub)

#### Offene Entscheidungen

1. Soll pro Task genau **ein** `RESPONSIBLE` erzwungen werden?
  - Soll nicht erzwungen werden, aber UI kann Warnung anzeigen, wenn. Es sollen aber nicht mehr als 1 sein.
2. Sollen Groups andere Groups enthalten dürfen (verschachtelte Teams)?
   - Erstmal nicht, aber später möglich.
3. Muss `Membership` historisiert werden oder reicht nur aktueller Zustand?
   - Aktueller Zustand reicht, Historie kann später ergänzt werden.
4. Soll Auth lokal (Passwort) starten oder direkt über externes IAM laufen?
   - Im Nicht-Test-Modus haben wir keycloak doch bereits im Einsatz, daher direkt externes IAM.
5. System-Default für Tageskapazität: 8h oder pro User verpflichtend pflegbar?
   - Ja, Teilzeit kommt vor.
6. Starten wir ohne wiederkehrende Regeln und führen diese erst später ein?
   - Ja, erst später.
7. Sollen Verfügbarkeiten nur für User gelten oder direkt auch für Groups pflegbar sein?
   - Erstmal nur für User, später auch für Groups.