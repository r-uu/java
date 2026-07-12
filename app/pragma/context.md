# Projektkontext: pragma (Java / Jakarta EE Backend & JavaFX Frontend)

## Hinweise für KI-Agenten

Diese Datei dient als zentrale Kontext-Datei für AI-Agenten wie Claude Code oder Gemini. Sie soll bei jedem Chat mit AI-Agenten berücksichtigt und automatisch aktuell gehalten werden.

Die [Übergeordnete Kontext-Datei](https://github.com/r-uu/java/blob/main/context.md) muss immer zuerst gelesen und berücksichtigt werden. Die Inhalte in der übergeordneten Datei werden durch den Inhalt dieser Datei ggf. überschrieben.

Die folgenden Dateien enthalten ebenfalls relevante Informationen für AI-Agenten zu Aspekten des Projekts pragma:

# [Analyse](doc/analysis/analysis.md)
# [Design](doc/design/design.md)
# [Implementierung](doc/implementation/implementation.md)

Diese Dateien sind von AI-Agenten ebenfalls zu lesen, zu beachten und aktuell zu halten.

# Maven / BOM

pragma hat ein eigenes BOM-Modul `bom` (`r-uu.app.pragma.bom`). Parent-Kette:
`r-uu.lib.bom` (aus lib/) ← `r-uu.app.pragma.bom` ← Root `r-uu.app.pragma` ← Submodule.

Das App-BOM erbt vom lib-BOM (Build-/Plugin-/Property-Config, externe Dependency-Versionen)
und legt zusätzlich fest:
- App-Modul-Versionen (`r-uu.app.pragma.*`) via `${project.version}` — folgen bewusst der App-Version.
- **Genutzte `r-uu.lib.*`-Module: direkt mit Klartext-lib-Version gepinnt** — entkoppelt die
  App-Version von der lib-Version (Variante B).

**Regel für AI-Agenten:** Wenn ein Modul ein *neues* `r-uu.lib.*`-Artefakt nutzt, muss dafür ein
direkter Pin in `bom/pom.xml` ergänzt werden. Ein `<scope>import</scope>` des lib-BOM genügt **nicht**
(importierte BOMs haben in Maven die niedrigste Priorität und werden vom geerbten `dependencyManagement`
überschrieben). Details siehe [Implementierung](doc/implementation/implementation.md).