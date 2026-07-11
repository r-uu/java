# Backlog — Schwachstellen und offene Punkte

Analysiert: 2026-06-28. Zuletzt aktualisiert: 2026-06-28.
Bezieht sich auf den aktuellen Stand der `main`-Branch.

Priorität: **P1** = scharfer Defekt oder Sicherheitsproblem · **P2** = verhindert verlässlichen Betrieb ·
**P3** = Codequality / Wartbarkeit · **P4** = strategisch, mittelfristig.

---

## P3 — Codequality und Wartbarkeit

### P3-1 · `jsonb` — Verwendung deprecated API `Strings.isNullOrEmptyOrBlank`

**Dateien:**
- `jsonb/src/main/java/de/ruu/lib/jsonb/AbstractSetAdapter.java`
- `jsonb/src/main/java/de/ruu/lib/jsonb/AbstractOptionalSetAdapter.java`

Beide Klassen importieren und rufen `Strings.isNullOrEmptyOrBlank` auf, obwohl diese Methode
als `@Deprecated` markiert wurde (Nachfolger: `Strings.isNullOrBlank`).

**Empfehlung:** Import und Aufruf auf `isNullOrBlank` umstellen.

---

## P4 — Fehlende Tests

Der Anteil testloser Module bleibt hoch für eine Bibliothek, die in Produktivprojekten
eingesetzt wird:

| Modul                                   | Kritikalität          | Status                                                                     |
|-----------------------------------------|-----------------------|----------------------------------------------------------------------------|
| `fx/comp` (FXC-Framework)               | hoch                  | offen (TestFX/headless Setup nötig)                                        |
| `jpa/se`                                | mittel                | offen (Persistence-Provider nötig)                                         |
| `cdi/common`                            | mittel                | offen (CDI-Laufzeit nötig)                                                 |
| `junit`                                 | mittel                | offen (`DisabledOnServerNotListeningCondition`, `HostPortConfigExtension`) |
| `mapstruct/spi`                         | niedrig               | offen (Annotation-Processor, kein sinnvoller Unit-Test)                    |
| `gen/java/fx/comp` (Generator)          | niedrig               | offen (FX-Laufzeit, kein einfacher Unit-Test)                              |
| `gen/java/fx/bean_editor` (Generator)   | niedrig               | offen (FX-Laufzeit, kein einfacher Unit-Test)                              |
| `cdi/demo` / `fx/comp_demo` / `fx/demo` | niedrig (Demo-Module) | offen, niedrige Priorität                                                  |

---

## P4 — Offene Design-TODOs im Code

### P4-1 · `gen/java/core/JavaTypeDeclared` — fehlender Wildcard-Typ-Support

**Datei:** `gen/java/core/src/main/java/de/ruu/lib/gen/java/JavaTypeDeclared.java`

TODO im Code: Support für `? extends Type` fehlt noch im Generator-Framework.

---

### P4-2 · `jpa/core/AbstractEntity` — ungeklärte Redundanz zur `Entity2`-Default-Implementierung

**Datei:** `jpa/core/src/main/java/de/ruu/lib/jpa/core/AbstractEntity.java` (Zeilen 49, 52)

Zwei TODO-Kommentare: Unklar, warum die Default-Implementierung im `Entity2`-Interface nicht
ausreicht und `AbstractEntity` dieselbe Logik nochmals überschreibt.

**Empfehlung:** Klären, ob die Überschreibungen tatsächlich notwendig sind; ggf. entfernen.

---

### P4-3 · `gen/java/core/CompilationUnitFileWriter` — fehlende Validierung in `root()`

**Datei:** `gen/java/core/src/main/java/de/ruu/lib/gen/java/CompilationUnitFileWriter.java` (Zeile 49)

TODO im Code: `root(Path)` nimmt den Pfad ohne Prüfung entgegen (existiert der Pfad? Ist er
beschreibbar?). Gleiches gilt für `CompilationUnitResourceFileWriter.java` (Zeile 51).

**Empfehlung:** Eingabe-Validierung oder zumindest einen klaren Vertrag im Javadoc ergänzen.

---

### P4-4 · `jpa/core/AbstractPersistenceUnitInfo` — ungeklärte DataSource-Verdoppelung

**Datei:** `jpa/core/src/main/java/de/ruu/lib/jpa/core/AbstractPersistenceUnitInfo.java` (Zeile 123)

TODO im Code: Die Klasse hält intern nur eine `DataSource`-Instanz, gibt diese aber sowohl
über `getJtaDataSource()` als auch über `getNonJtaDataSource()` zurück. Unklar, ob dieses
Verhalten für beide Transaktionsmodi (JTA und RESOURCE_LOCAL) korrekt ist.

**Empfehlung:** Klären, ob eine zweite `DataSource`-Instanz für JTA nötig ist oder ob die
einheitliche Rückgabe gewollt und dokumentiert werden soll.

---

### P4-5 · `gen/java/core/GeneratorCompilationUnit` — fehlende Datei-Schreib-Erweiterung

**Datei:** `gen/java/core/src/main/java/de/ruu/lib/gen/java/GeneratorCompilationUnit.java` (Zeile 12)

TODO im Javadoc: Das Interface soll um Funktionalität erweitert werden, die Generator-Output
komfortabel in das Dateisystem schreibt. Diese Funktionalität ist noch nicht implementiert.

**Empfehlung:** Convenience-Methoden analog zu `CompilationUnitFileWriter` ergänzen oder
die beiden Konzepte besser integrieren.

---

### P4-6 · `util/lang/model/JavaLangMetaModel` — ungeklärtes Visitor-Verhalten

**Datei:** `util/src/main/java/de/ruu/lib/util/lang/model/JavaLangMetaModel.java` (Zeile 51)

TODO im Code: Im `TypeElementProviderVisitor` (basierend auf `SimpleElementVisitor14`)
werden String-Listen und Integer-Objekte nicht als Typen besucht. Die Ursache ist ungeklärt.

**Empfehlung:** Ursache analysieren; ggf. Workaround oder Dokumentation ergänzen.
