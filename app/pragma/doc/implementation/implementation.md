# Projekt **pragma**

## Technologie-Stack

- **Backend:** Java 25, Jakarta EE 10, JPA (Hibernate), JAX-RS (Jersey auf dem Client)
- **Server:** Open Liberty 25.0.0.12, Datasource PostgreSQL via JDBC
- **Frontend:** JavaFX (in diesem Repo), Kotlin CMP (in `pragma-cmp`)
- **Build:** Maven (mit BOM aus `lib/`)
- **JSON:** Jackson mit Field-Visibility (FIELD=ANY, GETTER/SETTER/IS_GETTER=NONE)

## Technologie-Stack-Detail

### mapstruct

In der jeeeraaah Implementierung hat sich mapstruct nicht als sehr hilfreich erwiesen. Im pragma Projekt soll mapstruct auf den Prüfstand gestellt werden. Ein Verzicht auf mapstruct ist eine valide Option. AI-Agenten sollen dazu eine sinnvolle Einschätzung abgeben.

## Schichtenmodell

Tasks (und TaskGroups) durchlaufen mehrere Schichten, die sich möglichst analog verhalten sollen:

| Schicht   | Klasse          | Beschreibung                                      |
|-----------|-----------------|---------------------------------------------------|
| Bean/POJO | `TaskBean`      | Einfaches Java-Objekt ohne Framework              |
| JPA       | `TaskJPA`       | JPA-Entity mit Annotationen                       |
| DTO       | `TaskDto`       | Datentransferobjekt für REST                      |
| JavaFX    | `TaskFx`        | Observable-Objekt mit JavaFX Properties (geplant) |

## Namenskonventionen

- Methoden ohne `get`/`set`-Präfix: `id()`, `name()`, `parentTask()` usw.
- Fluent-Setter geben `this` zurück (kovariant: Interface-Typ im Interface, konkreter Typ in Implementierungen).
- Fluent-Setter für Singular-Relationen (`parentTask`, `taskGroup`) geben den Self-Type `T` zurück, da `T` als Typparameter verfügbar ist.
- Mutations-Methoden für Collections (`addSubTask`, `removePredecessor` …) geben `void` zurück.

## Optional-Semantik für Relationen

**To-one-Relationen** (`parentTask`, `taskGroup`) — JPA-Default: EAGER:
- `Optional.empty()` = kein Wert (Domänenfakt: Task ist Root-Task bzw. keiner Gruppe zugeordnet)
- `Optional.of(t)` = hat Wert

**To-many-Relationen** (`subTasks`, `predecessors`, `successors`, `tasks`) — JPA-Default: LAZY:
- `Optional.empty()` = noch nicht geladen (Infrastrukturfakt, null intern)
- `Optional.of(emptySet)` = geladen, leer
- `Optional.of(set)` = geladen, hat Elemente

Die Unterscheidung ist durch die Kardinalität der Relation eindeutig — keine zusätzliche Dokumentation am Aufrufpunkt nötig.

## Bidirektionale Relationen

Alle bidirektionalen Relationen werden durch explizite add/remove-Methoden verwaltet.
Die Methoden pflegen beide Seiten der Relation und sind gegen unendliche Rekursion gesichert
(`Set.add()` gibt `false` zurück, wenn das Element bereits vorhanden ist).

| Relation                  | Methoden                                    | gegenseitige Pflege                  |
|---------------------------|---------------------------------------------|--------------------------------------|
| parentTask / subTasks     | `addSubTask(T)`, `removeSubTask(T)`         | setzt parentTask auf der Kindseite   |
| predecessors / successors | `addPredecessor(T)`, `removePredecessor(T)` | registriert Successor auf Gegenseite |
| taskGroup / tasks         | `addTask(T)`, `removeTask(T)`               | setzt taskGroup auf der Task-Seite   |

## Interface-Hierarchie

Das geteilte Verhalten aller Schichten wird durch generische Interfaces erzwungen.

```java
// de.ruu.app.pragma.core — Basisinterfaces

public interface HasId<ID>    { ID     id();   }
public interface HasName      { String name(); }
public interface HasMutableName extends HasName
                              { HasMutableName name(String name); }

// T = Self-Type → Setter gibt T zurück, kein Cast nötig
public interface HasParentTask<T>        { Optional<T> parentTask();   }
public interface HasMutableParentTask<T>
        extends HasParentTask<T>         { T parentTask(T t);          }

// Optional<Set<T>>: empty = not loaded, of(set) = loaded
public interface HasSubTasks<T>          { Optional<Set<T>> subTasks();      }
public interface HasPredecessors<T>      { Optional<Set<T>> predecessors();  }
public interface HasSuccessors<T>        { Optional<Set<T>> successors();    }

public interface HasMutableSubTasks<T>   extends HasSubTasks<T>
                              { void addSubTask(T t);    void removeSubTask(T t);    }
public interface HasMutablePredecessors<T> extends HasPredecessors<T>
                              { void addPredecessor(T t); void removePredecessor(T t); }
public interface HasMutableSuccessors<T> extends HasSuccessors<T>
                              { void addSuccessor(T t);  void removeSuccessor(T t);  }

public interface HasTaskGroup<G extends TaskGroup<?>>
                              { Optional<G> taskGroup(); }
public interface HasMutableTaskGroup<G extends TaskGroup<?>>
        extends HasTaskGroup<G> { HasMutableTaskGroup<G> taskGroup(G g); }

// Nicht-generische Anker für schichtenübergreifende Collections
public interface RawTask      extends HasId<Long>, HasName {}
public interface RawTaskGroup extends HasId<Long>, HasName {}

// G = TaskGroup-Typ, T = eigener Typ (F-Bound)
// Wildcard im G-Bound nötig wegen bidirektionaler TaskGroup.tasks()-Referenz
public interface Task<G extends TaskGroup<? extends Task<G, ?>>, T extends Task<G, T>>
        extends RawTask, HasMutableName, HasMutableParentTask<T>,
                HasMutableSubTasks<T>, HasMutablePredecessors<T>, HasMutableSuccessors<T>,
                HasMutableTaskGroup<G> {}

public interface TaskGroup<T extends Task<?, ?>> extends RawTaskGroup, HasMutableName
{
    Optional<Set<T>> tasks();     // empty = not loaded
    void addTask(T task);
    void removeTask(T task);
}

// Read-only Persistenz-Metadaten-Vertrag über alle Schichten
public interface PersistentTask
        <G extends PersistentTaskGroup<? extends PersistentTask<G, ?>>, T extends PersistentTask<G, T>>
        extends Task<G, T>, Entity<Long> {}

public interface PersistentTaskGroup<T extends PersistentTask<?, ?>>
        extends TaskGroup<T>, Entity<Long> {}
```

### Implementierungen

| Schicht   | Task-Klasse     | TaskGroup-Klasse     | Modul                |
|-----------|-----------------|----------------------|----------------------|
| Bean/POJO | `TaskBean`      | `TaskGroupBean`      | `bean`               |
| JPA       | `TaskJPA`       | `TaskGroupJPA`       | `backend/jpa`        |
| DTO       | `TaskDto`       | `TaskGroupDto`       | `backend/dto`        |
| JavaFX    | `TaskFx`        | `TaskGroupFx`        | `frontend/fx` (geplant) |

### Trade-offs

`PersistentTask` / `PersistentTaskGroup` benennen bewusst keinen JPA-spezifischen Objekttyp,
sondern einen schichtübergreifenden, read-only Vertrag für DB-verwaltete Metadaten `id()` und
`version()`. Schreibzugriffe darauf erfolgen nicht per Setter, sondern ausschließlich über die
Persistenzschicht; andere Schichten transportieren diese Werte nur mit.

**Vorteil:** Generische Utilities (z. B. Baumtraversierung) funktionieren schichtenübergreifend:
```java
<G extends TaskGroup<? extends Task<G, ?>>, T extends Task<G, T>>
Set<T> collectLeaves(T root) { ... }
```

**Wildcard-Komplexität** entsteht zwingend durch die bidirektionale Referenz Task ↔ TaskGroup
(wie im jeeeraaah-Projekt). `RawTask`/`RawTaskGroup` dienen als Anker für `List<RawTask>`
über Schichtgrenzen ohne Wildcard.



# Projektumsetzung — aktueller Stand

## Repository-Struktur

| Repo          | Inhalt                                                                          |
|---------------|---------------------------------------------------------------------------------|
| `java`        | Libraries (`lib/`) + Jakarta EE Backend & JavaFX Frontend (`app/pragma/`) — dieses Repo |
| `pragma-cmp`  | Kotlin Compose Multiplatform Frontend                                           |

Das BOM-Modul (`r-uu.lib.bom`) lebt als Maven-Submodul in `lib/bom/` und verwaltet gemeinsame maven Dependency-Versionen für alle Projekte. Nicht alle Dependencies werden im BOM verwaltet — nur die projektübergreifend relevanten.

In pragma werden alle maven Dependency-Versionen nach Möglichkeit im BOM verwaltet.

## Maven-Modulstruktur

Package-Prefix: `de.ruu.app.pragma`

```
app/pragma/
├── pom.xml                       (r-uu.app.pragma, parent: r-uu.app.pragma.bom)
├── bom/                          (r-uu.app.pragma.bom)      — App-BOM, parent: r-uu.lib.bom
├── core/                         (r-uu.app.pragma.core)          — Interfaces (Task, TaskGroup, Has*)
├── bean/                         (r-uu.app.pragma.bean)          — POJO-Implementierung
├── backend/
│   ├── pom.xml                   (r-uu.app.pragma.backend)
│   ├── dto/                      (r-uu.app.pragma.backend.dto)           — DTOs für REST (Jackson)
│   ├── jpa/                      (r-uu.app.pragma.backend.jpa)           — JPA-Entities (Hibernate)
│   └── rest/                     (r-uu.app.pragma.backend.rest, WAR)     — JAX-RS REST-API + Liberty Server
│       └── src/main/liberty/config/
│           ├── server.xml        — Liberty-Konfiguration (Port 9090, Datasource jdbc/pragma)
│           └── server.env        — Umgebungsvariablen (Ports, DB-Credentials)
└── frontend/
    ├── pom.xml                   (r-uu.app.pragma.frontend)
    ├── rest-client/              (r-uu.app.pragma.frontend.rest.client)   — JAX-RS Client (Jersey)
    │   └── src/main/java/de/ruu/app/pragma/client/
    │       ├── TaskGroupClient.java
    │       ├── TaskClient.java
    │       └── dbcommand/        — DB-Hilfsprogramme (clear/populate)
    └── fx/                       (r-uu.app.pragma.frontend.fx, geplant)   — JavaFX UI
```

## BOM-Hierarchie und Versions-Entkopplung (Variante B)

pragma hat ein eigenes BOM-Modul `bom` (`r-uu.app.pragma.bom`). Parent-Kette:

```
r-uu.lib.bom        (lib/ — Build-/Plugin-/Property-Config, externe Dep-Versionen)
        ▲ parent
r-uu.app.pragma.bom (dieses Repo — App-Modul-Versionen + gepinnte lib-Versionen)
        ▲ parent
r-uu.app.pragma     (Root / Aggregator)
        ▲ parent
core / bean / backend/* / frontend/*   (deklarieren Dependencies ohne <version>)
```

Das App-BOM erbt das lib-BOM als Parent (damit Build-/Plugin-/Property-Konfiguration,
Repositories und externe Dependency-Versionen) und ist die zentrale Stelle für App-Versionen.
Submodule referenzieren App-interne und lib-Dependencies **ohne `<version>`** — die Version
kommt aus dem `dependencyManagement` des App-BOM.

### Ziel: App-Version unabhängig von der lib-Version

Das lib-BOM deklariert seine eigenen Module mit `<version>${project.version}</version>`. Wird das
**geerbt**, löst `${project.version}` gegen *dieses* (das App-)Projekt auf — die App-Version würde
also stillschweigend an die lib-Version gekoppelt.

Deshalb pinnt das App-BOM jedes tatsächlich genutzte `r-uu.lib.*`-Modul **direkt** auf die
lib-Version (Klartext, keine Property — gemäß Projektregel). Aktuell gepinnt:
`fx.comp`, `fx.core`, `jpa.core`, `junit`, `postgres.ui` (alle `0.0.1`).

Die App-Module selbst (`r-uu.app.pragma.*`) bleiben bei `${project.version}` und folgen damit
bewusst der App-Version.

### Warum kein `<scope>import</scope>`

Ein `import` des lib-BOM würde **nicht** funktionieren: Importierte BOMs haben in Maven die
niedrigste Priorität und werden von geerbtem bzw. direkt deklariertem `dependencyManagement`
überschrieben. Nur ein **direkt deklarierter** Eintrag überschreibt einen geerbten. Empirisch
verifiziert (effektiver POM mit testweise abweichender App-Version): via `import` folgt die
lib-Version der App-Version, via direktem Pin bleibt sie unabhängig.

### Regel beim Hinzufügen neuer lib-Dependencies

Nutzt ein Modul ein *neues* `r-uu.lib.*`-Artefakt, muss dafür ein direkter Pin in
`bom/pom.xml` ergänzt werden. Ohne Pin fällt es auf die geerbte App-Version zurück; sobald
App- und lib-Version divergieren, führt das zu einem Build-Fehler (Artefakt in App-Version nicht
vorhanden).

**DRY-Alternative (nicht umgesetzt):** Das lib-BOM in einen Build-Config-Parent und einen reinen
Versionskatalog aufteilen; die App könnte den Katalog dann per `import` beziehen und bräuchte keine
Pins. Das erfordert umfangreiche Änderungen in lib/.

## Open Liberty Server

- **Port:** 9090 (HTTP), 9543 (HTTPS) — um Konflikt mit jeeeraaah (9080) zu vermeiden
- **Context root:** `/pragma`, JAX-RS base: `/pragma/api`
- **Features:** `jakartaee-10.0`, `microProfile-6.1`
- **Datasource:** `jdbc/pragma` → PostgreSQL (Container `pragma-postgres`, Port 5432)
- **JPA provider:** Hibernate (JARs in `lib/global/`, nicht im WAR gebündelt)
- **Start:** `mvn -pl backend/rest liberty:run` (Produktion) oder `mvn -pl backend/rest liberty:dev` (Hot-Reload)

## Jackson-Konfiguration

Jackson ist als primärer JSON-Provider registriert (überschreibt Liberty's Yasson/JSON-B):

```java
// PragmaApplication.getSingletons()
ObjectMapper mapper = new ObjectMapper()
    .registerModule(new Jdk8Module())
    .registerModule(new JavaTimeModule())
    .setVisibility(...withFieldVisibility(ANY).withGetterVisibility(NONE)...)
```

DTOs benötigen:
- Protected no-arg Konstruktor (für Jackson-Deserialisierung)
- `opens de.ruu.app.pragma.dto;` in `module-info.java` (für reflektiven Feldzugriff)

## REST-Endpoints

Basis-URL: `http://localhost:9090/pragma/api`

| Methode  | Pfad                               | Beschreibung                                       |
|----------|------------------------------------|----------------------------------------------------|
| GET      | `/task-groups`                     | Alle Gruppen                                       |
| GET      | `/task-groups/{id}`                | Gruppe nach ID                                     |
| POST     | `/task-groups`                     | Neue Gruppe                                        |
| PUT      | `/task-groups/{id}`                | Gruppe umbenennen                                  |
| DELETE   | `/task-groups/{id}`                | Gruppe löschen                                     |
| DELETE   | `/task-groups`                     | Alle Gruppen + Tasks löschen (inkl. Join-Tabellen) |
| GET      | `/tasks?groupId=`                  | Tasks (optional gefiltert nach Gruppe)             |
| GET      | `/tasks/{id}`                      | Task nach ID                                       |
| GET      | `/tasks/{id}/with-related`         | Task mit subTasks, predecessors, successors (eager)|
| GET      | `/tasks/group/{groupId}/with-related` | Alle Tasks einer Gruppe mit Relationen (eager)  |
| POST     | `/tasks`                           | Neuer Task                                         |
| PUT      | `/tasks/{id}`                      | Task umbenennen                                    |
| PUT      | `/tasks/{id}/group/{groupId}`      | Task in andere Gruppe verschieben                  |
| PUT      | `/tasks/{id}/parent/{parentId}`    | Eltern-Task setzen                                 |
| DELETE   | `/tasks/{id}/parent`               | Eltern-Task aufheben                               |
| PUT      | `/tasks/{id}/predecessor/{predId}` | Vorgänger hinzufügen                               |
| DELETE   | `/tasks/{id}/predecessor/{predId}` | Vorgänger entfernen                                |
| DELETE   | `/tasks/{id}`                      | Task löschen                                       |

## JPMS-Entscheidungen

- **Named modules** (mit `module-info.java`): `core`, `bean`, `dto`, `jpa`, `rest-client` (Modulname: `de.ruu.app.pragma.client`), `fx`
  - Hyphene sind in JPMS-Modulnamen verboten — `rest-client` heißt deshalb `de.ruu.app.pragma.client`
  - Jersey (`jersey.client`) und MicroProfile Config (`microprofile.config.api`) sind automatische Module (Modulname aus JAR-Dateiname abgeleitet)
- **Unnamed modules** (ohne `module-info.java`): `backend/rest` (WAR)
  - Grund: Liberty WAR-Deployment nutzt eigenes Classloading, JPMS nicht sinnvoll

## Hinweise

- Das lib-BOM (`r-uu.lib.bom:0.0.1`) muss vor dem Build dieses Projekts im lokalen Maven-Repository vorhanden sein (`mvn install` aus dem Repo-Root oder aus `lib/`).
- `backend/rest/src/main/liberty/config/lib/global/` ist gitignored — Hibernate-JARs werden beim Build durch `maven-dependency-plugin` dorthin kopiert.
- `docker-compose.yml` im Projektroot startet PostgreSQL 17 (Container `pragma-postgres`).

# Planung

## JavaFX Frontend um JavaFXSmartGraph erweitern

Empfehlung: Setze auf JavaFXSmartGraph. Es spart dir bei der Implementierung von komplexen Netzbeziehungen (wie deinen Predecessor-/Successor-Abfolgen) Wochen an Arbeit, die du bei FXGraph in das Schreiben eigener Layout-Algorithmen stecken müsstest. Nimm dazu die Bibliothek JGraphT für die mathematische Zyklerkennung im Hintergrund, füttere das Ergebnis in JavaFXSmartGraph ein, und du hast eine extrem robuste und moderne UI-Erweiterung für deinen Client.

## Aufbau der FX UI

Die pragma-UI soll sich an der jeeeraaah-UI orientieren. Sie soll insbesondere das FXC-Framework aus lib/ verwenden. Einstieg in die pragma-UI soll eine FXC-App sein, die zunächst folgende Kacheln anzeigt:

- Hierarchy View: zeigt FXCView Hierarchies (orientiert sich an de.ruu.app.jeeeraaah.frontend.ui.fx.task.hierarchy.Hierarchies)
- Gantt View:     zeigt FXCView Gantt       (orientiert sich an de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt.Gantt)
- Graph View:     zeigt FXCView Graph, wird mit JavaFXSmartGraph neu erstellt

Die Views sollen analog zur jeeeraaah Version autonom als FXCApp lauffähig sein.

### Aktueller Stand (FX UI)

Alle drei FXC-Views sowie die Haupt-App sind implementiert in `frontend/fx`:

#### Hierarchies View (`de.ruu.app.pragma.fx.task.hierarchy`)
| Klasse                  | Rolle                                                                 |
|-------------------------|-----------------------------------------------------------------------|
| `HierarchiesService`    | FXCService-Interface                                                  |
| `Hierarchies`           | FXCView — lädt `Hierarchies.fxml`                                     |
| `HierarchiesController` | 3-Panel-Layout: predecessor ← task → successor (TreeViews, RTL links) |
| `HierarchiesApp`        | FXCApp standalone                                                     |
| `HierarchiesAppRunner`  | `main()` entry point                                                  |

#### Gantt View (`de.ruu.app.pragma.fx.task.gantt`)
| Klasse              | Rolle                                                                    |
|---------------------|--------------------------------------------------------------------------|
| `GanttService`      | FXCService-Interface                                                     |
| `Gantt`             | FXCView — lädt `Gantt.fxml`                                              |
| `GanttController`   | TreeTableView: Namensspalte + Tagesspalten (blau = planned); Datumeditor |
| `de.ruu.app.pragma.fx.task.WebViewJavaScriptGanttApp`          | FXCApp standalone                                                        |
| `GanttAppRunner`    | `main()` entry point                                                     |

Voraussetzung: `TaskDto`/`TaskJPA` haben `plannedStart`/`plannedEnd` (LocalDate, nullable).
REST-PUT `/tasks/{id}` persistiert diese Felder. `hbm2ddl.auto=update` fügt Spalten automatisch hinzu.

#### Graph View (`de.ruu.app.pragma.fx.task.graph`)
| Klasse              | Rolle                                                                              |
|---------------------|------------------------------------------------------------------------------------|
| `GraphService`      | FXCService-Interface                                                               |
| `Graph`             | FXCView — lädt `Graph.fxml`                                                        |
| `GraphController`   | Reines JavaFX-Graph: TaskBean-Knoten als abgerundete Rechtecke (Rectangle + VBox), |
|                     | Kanten als Line+Polygon, einmaliges topologisches Auto-Layout, dann nur Dragging.  |
|                     | Ein REST-Call (`findGroupTasksWithRelated`) lädt alle Tasks mit Relationen.         |
| `GraphApp`          | FXCApp standalone                                                                  |
| `GraphAppRunner`    | `main()` entry point                                                               |

Kein SmartGraph für das visuelle Rendering (SmartGraph 2.0.0 unterstützt keine Rechtecke).
SmartGraph-Dependency bleibt im Classpath, wird aber nicht mehr aktiv genutzt.

#### Haupt-App (`de.ruu.app.pragma.fx`)
| Klasse              | Rolle                                |
|---------------------|--------------------------------------|
| `PragmaService`     | FXCService-Interface                 |
| `Pragma`            | FXCView — lädt `Pragma.fxml`         |
| `PragmaController`  | TabPane mit 3 Tabs: Hierarchies, Gantt, Graph (eingebettet via AnchorPane) |
| `PragmaApp`         | FXCApp — Haupteinstiegspunkt         |
| `PragmaAppRunner`   | `main()` — startet Weld CDI + JavaFX |

**IntelliJ Run Configs (exec-maven-plugin executions):**
- `pragma fx:hierarchies` → `HierarchiesAppRunner`
- `pragma fx:gantt`       → `GanttAppRunner`
- `pragma fx:graph`       → `GraphAppRunner`
- `pragma fx:pragma`      → `PragmaAppRunner` (Haupt-App mit allen 3 Views)

#### Hinweise zur Implementierung

- `HierarchiesController.pickTask()` lädt per `taskClient.findAll()` bei jedem Aufruf frische Daten — kein veralteter Cache-Stand im Vorgänger/Nachfolger-Dialog.
- `GraphController` speichert/lädt Knotenpositionen als `.pgraph`-Datei (Properties-Format, Task-ID als Schlüssel). Wird nach `DBClear`+`DBPopulate` wertlos, da neue IDs vergeben werden (→ Backlog P2-6).
- SmartGraph ist als Dependency deklariert, wird aber nicht für das Rendering verwendet (→ Backlog P3-6).
