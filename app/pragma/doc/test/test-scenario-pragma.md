erstelle eine ausführbare klasse namens SetupPragmaTestScenarioBaseline, die die die umgebung für das unten beschriebene test scenario erzeugt.

- SetupPragmaTestScenarioBaseline sichert zu beginn seiner ausführung die aktuellen pragma daten

    - psstgres

        - über die klasse PostgresToolBox im projekt lib werden die pragma postgres daten gesichert. die pragma postgres backup daten werden im verzeichnis java/app/pragma/dbbackup gespeichert. die backup datei wird mit dem aktuellen datum und der aktuellen uhrzeit benannt, z.b. "backup_pragma_postgres_2023-03-15_14-30-00.dump".

    - keycloak

        - zusätzlich zu den pragma postgres daten werden auch die pragma keycloak daten gesichert. die pragma keycloak daten werden ebenfalls im verzeichnis java/app/pragma/dbbackup gespeichert. die backup datei wird mit dem aktuellen datum und der aktuellen uhrzeit benannt, z.b. "backup_pragma_keycloak_2023-03-15_14-30-00.dump".
        - Es muss noch geklärt werden, wie die keycloak daten gesichert und wiederhergestellt werden können. ggf. muss die keycloak datenbank in der keycloak docker-compose.yml datei auf eine externe datenbank umgestellt werden, damit die keycloak datenbank mit dem postgres-toolbox backup-tool gesichert werden kann.
 
 

    - es ist von entscheidender wichtigkeit, dass pragma postgres und pragma keycloak daten zueinander passen. dies muss durch SetupPragmaTestScenarioBaseline sichergestellt werden.

- direkt nach der Sicherung aller daten (s.o.) löscht SetupPragmaTestScenarioBaseline sowohl die aktuellen postgres als auch die aktuellen keycloak daten.
- die klasse erstellt eine neue testumgebung mit den folgenden daten:
  - name: "pragma test scenario baseline complete"
  - description: "this is a test scenario for pragma"
  - created_at: current date and time
  - updated_at: current date and time