erstelle eine ausführbare klasse namens SetupPragmaTestScenarioBaseline, die die die umgebung für folgendes test scenario erzeugt.

- SetupPragmaTestScenarioBaseline sichert zu beginn ihrer ausführung die aktuellen pragma daten

    - über die klasse PostgresToolBox im projekt lib werden die pragma postgres daten gesichert. die pragma postgres backup daten werden im verzeichnis java/app/pragma/dbbackup gespeichert. die backup datei wird mit dem aktuellen datum und der aktuellen uhrzeit benannt, z.b. "backup_pragma_postgres_2023-03-15_14-30-00.dump".

    - zusätzlich zu den pragma postgres daten werden auch die pragma keycloak daten gesichert. die pragma keycloak daten werden ebenfalls im verzeichnis java/app/pragma/dbbackup gespeichert. die backup datei wird mit dem aktuellen datum und der aktuellen uhrzeit benannt, z.b. "backup_pragma_keycloak_2023-03-15_14-30-00.dump".

    - es ist von entscheidender wichtigkeit, dass pragma postgres und pragma keycloak daten zueinander passen. dies muss durch SetupPragmaTestScenarioBaseline sichergestellt werden.
#
- danach werden sowohl die aktuellen postgres daten als auch die aktuellen keycloak daten gelöscht.
- die klasse erstellt eine neue testumgebung mit den folgenden daten:
  - name: "pragma test scenario baseline complete"
  - description: "this is a test scenario for pragma"
  - created_at: current date and time
  - updated_at: current date and time