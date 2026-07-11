# Projektkontext: lib-java

## Hinweise für KI-Agenten

Diese Datei dient als Kontext-Datei für AI-Agenten wie Claude Code oder Gemini. Sie soll bei jedem Chat mit AI-Agenten berücksichtigt und automatisch aktuell gehalten werden. Insbesondere sollen hier wichtige Architekturentscheidungen automatisch aber im Dialog mit dem Entwickler dokumentiert und aktuell gehalten werden.

# Projektziele

Ziel des Projekts ist die Bereitstellung allgemein wiederverwendbarer Java-Libraries. Diese Datei beschreibt allgemein verbindliche Regeln, Zielsetzungen und Vorgehensweisen.

# Regeln

## Projektgrenzen

Das Projekt lehnt sich zwar an das alte `java/main/root/lib`-Projekt an, definiert aber einen klaren Schnitt: es soll keine Abhängigkeiten zu `java/main/root/lib` geben. Insbesondere sollen die Projekte unter `java/main/root` nicht von den Projekten unter `lib-java` abhängig sein.

Die Trennung gilt in beide Richtungen:
- `lib-java` darf keine Abhängigkeiten zu Projekten unter `java/main/root/lib` einführen.
- Projekte unter `java/main/root` sollen nicht von Projekten unter `lib-java` abhängig sein.

## Dokumentation

Dokumentation muss immer klar, präzise und gut verständlich sein. Dokumentation in .md Dateien ist deutsch, in Quellcode englisch.

## maven-multi-projects

Die bereitgestellten maven modules müssen immer so gewählt werden, dass sie bei Verwendung möglichst wenig, also nur die wirklich benötigten Abhängigkeiten einführen.

### Bill of Material

Alle Maven Module nutzen die Abhängigkeiten aus dem Modul [r-uu.bom](bom/pom.xml). Dort werden auch die (default) dependency-versions festgelegt.

#### Nutzung von properties im bom

Versions werden ohne die Nutzung von properties, also im Klartextformat angegeben. Das gilt für dependencies wie für plugins.

## Arbeit mit AI-Agenten

AI-Chat Fenster sollen immer mit einem light theme und / oder mit hohen Kontrasten arbeiten.

Projekte, insbesondere maven-sub-module, können eigene, ergänzende context.md Dateien beinhalten, die von KI-Agenten wie diese behandelt werden. Ggf. überschreiben Kontextdateien "untergeordneter" Projekte die Festlegungen in "übergeordneten" Projekten.

### Quality Assurance

AI-Agenten sollen überprüfen, ob Code durch weitere Tests besser abgesichert werden kann. Ist dies der Fall, sollen in Absprache mit dem Entwickler neue Tests generiert oder existierende Tests erweitert bzw. angepasst werden.

## Modularisierung

Wenn möglich und sinnvoll soll jpms eingesetzt werden, und dies möglichst konsequent.

## Code Formatting

Versuche aus meinem eigenen code die formatting rules abzuleiten.

## Code Style

### Einrückungen

Einrückungen sollen als 2 spaces sichtbar sein, entweder durch 2 space zeichen oder durch einen tab, der mit zwei spaces angezeigt wird.

### Fluent Style

Wenn möglich, soll für soll fluent style verwendet werden.

### Builder Pattern

Wenn sinnvoll, soll das builder-pattern anstelle von Konstruktoren verwendet werden.

## Persistenz

### JPA

Wenn möglich, immer das JPA-Criteria-API, ggf. zusammen mit dem Specificator-Pattern (aber ohne Spring-Data) verwenden. Queries by Example sind akzeptabel. Native queries nach Möglichkeit vermeiden.

# Vorgehensweisen