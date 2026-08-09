# JPMS in Aktion - das Proof-of-Concept-Projekt pragma

JPMS (Java Platform Module System) ist die Modultechnologie von Java. Sie wurde mit Java 9 eingeführt und ist in pragma ein zentrales Mittel, um die Architektur der Anwendung explizit und prüfbar zu machen.

Für das JDK selbst wird **JPMS** meist als großer Erfolg gewertet, da es seit dem nicht mehr als ein einziger riesiger Monolith (rt.jar) ausgeliefert werden muss, der schon aufgrund seiner Größe nicht mehr zum sich immer weiter verbreitenden Architekturmodell Microservices passte.

In der Java User Community hingegen kämpft JPMS aus verschiedenen Gründen weiter um Akzeptanz:

- Der oft nicht zu vermeidende Einsatz von (noch) nicht modularem legacy code erschwert den Einsatz von JPMS (Stichwort "split packages"). Der Nutzen ist dann ohnehin eingeschränkt: nicht modularer code "landet" in "automatic modules", die zwar auch Module heißen, aber nicht die Vorteile von JPMS Modulen mit sich bringen (dazu später mehr).
- Probleme mit reflection, die mit JPMS gesondert behandelt werden muss.
- Für die effektive Nutzung der JPMS features muss auch eine gewisse Lernkurve in Kauf genommen werden.

Dabei ist Modularisierung ein entscheidender Faktor für die Entwicklung von gut wartbaren, gut verständlichen und gut erweiterbaren, großen Softwaresystemen (siehe den Beitrag [modular software in java](../modular-software-in-java/modular-software-in-java.md)).

pragma wurde als Proof of Concept gestartet, um zu prüfen, ob sich JPMS auch in einer nicht trivialen Enterprise-Java-Anwendung sinnvoll einsetzen lässt. Dabei geht es nicht um Dogma, sondern um die praktische Frage, wo Modularisierung echten Mehrwert liefert und wo eine klassischere Struktur angemessener ist.

Gleichzeitig soll kritisch geprüft werden, ob die Vorteile von Modularisierung mit JPMS die Nachteile überwiegen, z. B. die Komplexität der Modularisierung selbst, die Komplexität der Build- und Deployment-Prozesse, ... .

Fachlich geht es im Projekt pragma im Kern um die Verwaltung von Aufgaben (`Task`s) und die Planung von Arbeitsabläufen. Dazu sollen zusammengehörige `Task`s in Gruppen (`TaskGroup`s) organisiert werden. **Abb. 1** zeigt das zentrale Objektmodell:

<p align="center">
  <img src="pragma-uml-taskgroup-task.drawio.svg" alt="TaskGroup - Task" width="350"/>
  <br/>
  <em>Abb. 1: TaskGroup und Task</em>
</p>

Die Idee ist, Aufgaben in Teilaufgaben zu gliedern (Tasks und SubTasks) und für alle Aufgaben Abläufe (Predecessor- und Successor-Tasks) planen zu können.

<p align="center">
  <img src="pragma-uml-task-objects.drawio.svg" alt="Task-Objects" width="350"/>
  <br/>
  <em>Abb. 2: Task-Objekte</em>
</p>

In der Anwendung sieht das dann im dashboard etwa so aus:

<p align="center">
  <img src="pragma-dashboard.png" alt="Task-Objects" width="350"/>
  <br/>
  <em>Abb. 3: pragma dashboard</em>
</p>

Eine Gantt-Diagramm-Darstellung zeigt eine andere Sicht auf Aufgaben und die geplanten Abläufe:

<p align="center">
  <img src="pragma-gantt.png" alt="Task-Objects" width="350"/>
  <br/>
  <em>Abb. 4: pragma Gantt Diagramm</em>
</p>

## Technologiestack

> Ein Ziel des POCs ist, die Versionen der eingesetzten Technologien dauerhaft auf einem möglichst modernen Stand zu halten. Updates aller Technologien gehören daher zur Tagesordnung.

pragma ist eine client-server Java Anwendung, deren Bestandteile (bis auf eine Ausnahme, dazu später mehr) mit Java 25 entwickelt wurden. Dabei kommen aktuell folgende Technologien zum Einsatz:

Das Backend ist eine Jakarta EE 10 / Microprofile 7.0 Anwendung. Als Application Server wird Open Liberty verwendet. Im Frontend kommt JavaFX 25 zum Einsatz.

Frontend und Backend sind weitestgehend mit jpms modularisiert. Die Kommunikation zwischen ihnen erfolgt über REST APIs, die mit Jakarta-RS implementiert wurden. Die (De-) Serialisierung der Daten erfolgt mit **Jackson**, was einen komfortablen und gleichzeitig effizienten Umgang auch mit zirkulären Datenstrukturen (siehe `Task`/`TaskGroup` Objektmodell) erlaubt. Die build Prozesse für beide Anwendungen werden mit Apache Maven realisiert.

Für das Identity and Access Management (IAM) wird Keycloak verwendet. Das frontend kommuniziert direkt mit Keycloak, um die Authentifizierung der Benutzer durchführen zu lassen. Das Open Liberty backend ist so konfiguriert, dass es die von Keycloak ausgestellten Token akzeptiert und die Autorisierung für alle eingehenden Requests durchführen kann.

Die persistente Datenhaltung im Backend wird mit einer Postgres Datenbank realisiert. Sie wird genau wie Keycloak in einem von `docker-compose` orchestrierten Container betrieben. In diesem POC liegen die **jeeeraaah**- zusammen mit den **keycloakk**-Daten in ein und derselben Datenbank, sie sind aber jeweils explizit einem eigenen Schema zugeordnet. Die **jeeeraaah** Zugriffe auf die Datenbank sind durchgängig mit JPA (hibernate) umgesetzt.

- Java 25
- Jakarta EE 10 / MicroProfile 6.1
- Open Liberty
- JavaFX 25
- Maven
- Jackson
- Hibernate
- PostgreSQL
- Keycloak
- Docker Compose

Das Backend läuft auf Open Liberty. Das Frontend kommuniziert über REST mit dem Backend und spricht für die Authentifizierung direkt mit Keycloak. Die Datenhaltung erfolgt in PostgreSQL; der Zugriff im Backend ist durchgängig mit JPA umgesetzt.

## Modulstruktur

```text
pragma/
├── core/                         # gemeinsames Domänenmodell und Basistypen
├── bean/                         # fluente Bean-Implementierungen
├── backend/
│   ├── dto/                      # DTOs für den Datentransfer
│   ├── jpa/                      # JPA-Entities
│   └── rest/                     # JAX-RS-WAR, bewusst ohne JPMS
└── frontend/
    ├── rest-client/              # REST-Client und Hilfsbefehle
    └── fx/                       # JavaFX-Oberfläche
```

Bis auf `r-uu.app.pragma.backend.rest` sind die Code-Module mit JPMS modularisiert. `backend.rest` bleibt bewusst ein WAR, weil **Open Liberty** es klassisch auf dem classpath deployt.

## Architektur

Die Architektur ist in wenige, klar getrennte Module aufgeteilt:

| Modul | Aufgabe |
|---|---|
| `core` | Domäneninterfaces, Basistypen und gemeinsame Verträge |
| `bean` | fluente, frameworkfreie Implementierungen der Domäneninterfaces |
| `backend.dto` | REST-DTOs |
| `backend.jpa` | JPA-Entities |
| `backend.rest` | JAX-RS-Ressourcen, Security und Application-Bootstrap |
| `frontend.rest-client` | REST-Client, Authentifizierung und Hilfsbefehle |
| `frontend.fx` | JavaFX-UI und dialogorientierte Oberflächenlogik |

Die zentralen Domänenobjekte sind `Task` und `TaskGroup`. Sie existieren in mehreren Schichten als schichtspezifische Implementierungen desselben Modellvertrags: Bean, JPA, DTO und JavaFX.

## Was JPMS hier bringt

- **Klare Grenzen:** Nur explizit exportierte Packages sind von außen sichtbar.
- **Frühe Fehlererkennung:** Fehlende Abhängigkeiten oder verbotene Zugriffe fallen zur Compile-Zeit auf.
- **Keine Split Packages:** Ein Package gehört eindeutig zu genau einem Modul.
- **Gezielte Reflection:** Frameworks bekommen nur die Packages, die sie wirklich brauchen.
- **Bessere Wartbarkeit:** Die `module-info.java` Dateien dokumentieren die Architektur direkt im Code.

## Aktueller Stand

| Kennzahl | Wert |
|---|---|
| JPMS-Module | 6 |
| Exportierte Packages | 14 |
| `opens`-Direktiven | 12 |
| Qualifizierte `opens` | 0 |
| Öffentliche Typen im modularisierten Teil | 104 |
| Davon intern verborgen | 7 |

Die meisten `opens`-Direktiven sind unqualifiziert, weil die betroffenen Packages nur für Laufzeit-Frameworks wie CDI, FXML oder Jackson geöffnet werden müssen. Eine qualifizierte Freigabe ist im aktuellen Stand nicht nötig.

## Pragmatische Ausnahme: `backend.rest`

`backend.rest` ist bewusst nicht als JPMS-Modul ausgeführt. Der Grund ist nicht fehlende Modularisierungsfähigkeit, sondern die Deploy-Zielplattform: Das WAR wird von **Open Liberty** klassisch betrieben, und der zusätzliche JPMS-Aufwand würde hier keinen praktischen Nutzen bringen.

## Fazit

pragma nutzt JPMS dort, wo es Struktur schafft, und verzichtet dort darauf, wo es nur Komplexität hinzufügen würde. Genau diese Balance macht die Modularisierung in diesem Projekt nützlich: klare Schnittstellen, weniger unbeabsichtigte Abhängigkeiten und ein Architekturmodell, das sich direkt am Code ablesen lässt.
