# Projektkontext: r-uu-java

## Hinweise für KI-Agenten

Diese Datei dient als zentrale Kontext-Datei für AI-Agenten wie Claude Code oder Gemini. Sie soll bei jedem Chat berücksichtigt und automatisch aktuell gehalten werden.

Projekte bzw. Maven-Sub-Module können eigene, ergänzende `context.md`-Dateien enthalten, die ggf. die Festlegungen dieser Datei überschreiben.

## Struktur

```
r-uu-java/
├── pom.xml              root-Aggregator (r-uu.java)
├── lib/                 wiederverwendbare Libraries (r-uu.lib.*)
│   ├── bom/             Bill of Material — Wurzel aller Build-Konfiguration
│   ├── archunit/
│   ├── cdi/
│   ├── fx/
│   ├── gen/
│   ├── jackson/
│   ├── jdbc/
│   ├── jpa/
│   ├── jsonb/
│   ├── junit/
│   ├── docker/
│   ├── keycloak/
│   ├── liberty/
│   ├── mapstruct/
│   ├── mp/
│   ├── office/
│   ├── postgres/
│   ├── util/
│   └── ws/
└── app/
    ├── pom.xml          app-Aggregator (r-uu.app)
    └── pragma/          Jakarta EE Backend + JavaFX Frontend (r-uu.app.pragma.*)
        ├── bom/
        ├── core/
        ├── bean/
        ├── backend/
        └── frontend/
```

## Maven-Parent-Kette

```
r-uu.lib.bom          (lib/bom/pom.xml — keine Parent, Wurzel)
  ↑ parent
r-uu.lib              (lib/pom.xml — Aggregator für alle lib-Module)
  + alle lib-Module erben von r-uu.lib.bom

r-uu.lib.bom
  ↑ parent
r-uu.app.pragma.bom   (app/pragma/bom/pom.xml)
  ↑ parent
r-uu.app.pragma       (app/pragma/pom.xml — Aggregator für pragma)
  + alle pragma-Module erben von r-uu.app.pragma

r-uu.lib.bom
  ↑ parent
r-uu.java             (pom.xml — root-Aggregator, modules: lib + app)
r-uu.app              (app/pom.xml — app-Aggregator, modules: pragma)
```

## Maven / BOM — wichtige Regeln

Das App-BOM (`r-uu.app.pragma.bom`) erbt vom Lib-BOM (`r-uu.lib.bom`). Dadurch erbt es:
- Build-/Plugin-/Property-Konfiguration
- externe Dependency-Versionen

**Regel für lib-Module:** Alle Module in `lib/` erben direkt oder transitiv von `r-uu.lib.bom`.
Versions werden als Klartext angegeben, keine Properties.

**Regel für pragma-Module:** Wenn ein pragma-Modul ein neues `r-uu.lib.*`-Artefakt nutzt,
muss in `app/pragma/bom/pom.xml` ein direkter Pin mit der Klartext-lib-Version ergänzt werden.
Ein `<scope>import</scope>` des lib-BOM genügt **nicht** (hat die niedrigste Maven-Priorität).

## Bootstrap — Erster Build ohne lokales Repository

`mvn clean install` vom Root funktioniert auch mit leerem `.m2`, weil:

1. Alle `<parent>`-Sektionen, die `r-uu.lib.bom` referenzieren, haben `<relativePath>` gesetzt —
   Maven löst das BOM direkt von Disk auf, ohne es aus `.m2` zu laden.
2. `bom` steht als erstes `<module>` in `lib/pom.xml`, damit es im Reaktor verfügbar ist,
   bevor die anderen Module gebaut werden.

Konkrete `<relativePath>`-Werte:

| pom.xml | relativePath |
|---|---|
| `pom.xml` (root) | `lib/bom/pom.xml` |
| `lib/pom.xml` | `bom/pom.xml` |
| `lib/*/pom.xml` (alle lib-Submodule) | `../bom` |
| `app/pom.xml` | `../lib/bom/pom.xml` |
| `app/pragma/bom/pom.xml` | `../../../lib/bom/pom.xml` |

## Allgemeine Regeln

Die Regeln aus `lib/context.md` (ehemals `lib-java/context.md`) gelten projektübergreifend.

## Herkunft

Dieses Repository entstand aus der Zusammenführung von:
- `lib-java` → `lib/` (Libraries)
- `app-pragma-java` → `app/pragma/` (Pragma-Applikation)

`java/main` wird bis zur Stilllegung weiter parallel betrieben.
