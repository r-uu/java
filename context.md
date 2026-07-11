# Projektkontext: r-uu-java

## Hinweise für KI-Agenten

Diese Datei dient als zentrale Kontext-Datei für AI-Agenten wie Claude Code oder Gemini. Sie soll bei jedem Chat berücksichtigt und automatisch aktuell gehalten werden.

Projekte bzw. Maven-Sub-Module können eigene, ergänzende `context.md`-Dateien enthalten, die ggf. die Festlegungen dieser Datei überschreiben.

## Struktur

```
r-uu-java/
├── pom.xml              root-Aggregator (r-uu.java)
├── lib/                 wiederverwendbare Libraries (r-uu.lib.java.*)
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
    ├── pom.xml          app-Aggregator (r-uu.java.app)
    └── pragma/          Jakarta EE Backend + JavaFX Frontend (r-uu.app.java.pragma.*)
        ├── bom/
        ├── core/
        ├── bean/
        ├── backend/
        └── frontend/
```

## Maven-Parent-Kette

```
r-uu.lib.java.bom (lib/bom/pom.xml — keine Parent, Wurzel)
  ↑ parent
r-uu.lib.java    (lib/pom.xml — Aggregator für alle lib-Module)
  + alle lib-Module erben von r-uu.lib.java

r-uu.lib.java.bom
  ↑ parent
r-uu.app.java.pragma.bom  (app/pragma/bom/pom.xml)
  ↑ parent
r-uu.app.java.pragma      (app/pragma/pom.xml — Aggregator für pragma)
  + alle pragma-Module erben von r-uu.app.java.pragma

r-uu.lib.java.bom
  ↑ parent
r-uu.java        (pom.xml — root-Aggregator, modules: lib + app)
r-uu.java.app    (app/pom.xml — app-Aggregator, modules: pragma)
```

## Maven / BOM — wichtige Regeln

Das App-BOM (`r-uu.app.java.pragma.bom`) erbt vom Lib-BOM (`r-uu.lib.java.bom`). Dadurch erbt es:
- Build-/Plugin-/Property-Konfiguration
- externe Dependency-Versionen

**Regel für lib-Module:** Alle Module in `lib/` erben direkt oder transitiv von `r-uu.lib.java.bom`.
Versions werden als Klartext angegeben, keine Properties.

**Regel für pragma-Module:** Wenn ein pragma-Modul ein neues `r-uu.lib.java.*`-Artefakt nutzt,
muss in `app/pragma/bom/pom.xml` ein direkter Pin mit der Klartext-lib-Version ergänzt werden.
Ein `<scope>import</scope>` des lib-BOM genügt **nicht** (hat die niedrigste Maven-Priorität).

## Allgemeine Regeln

Die Regeln aus `lib/context.md` (ehemals `lib-java/context.md`) gelten projektübergreifend.

## Herkunft

Dieses Repository entstand aus der Zusammenführung von:
- `lib-java` → `lib/` (Libraries)
- `app-pragma-java` → `app/pragma/` (Pragma-Applikation)

`java/main` wird bis zur Stilllegung weiter parallel betrieben.
