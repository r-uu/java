# Hetzner + Spaceship + Fritzbox: Setup-Dokumentation

Diese Dokumentation beschreibt die bisherige Einrichtung, den Zweck der einzelnen Schritte und die häufigsten Abläufe für Betrieb und Login.

## 1. Zielbild

Ein Hetzner-Server (CX23) hostet zwei Dienste hinter zwei Subdomains:

- `dms.r-uu.com` (als Oberbegriff für dein DMS)
- `pragma.r-uu.com`

Beide Subdomains zeigen auf denselben Server. Die Trennung erfolgt später per Reverse Proxy (z. B. Caddy).

### 1.1 Gesamtsetup im Überblick

Es gibt aktuell drei relevante Systeme:

| System | Zweck | Wo es läuft |
|---|---|---|
| Lokaler Rechner mit WSL | Administration per SSH, Pflege der Konfiguration | bei dir zuhause |
| Spaceship | DNS für `r-uu.com`, `dms.r-uu.com`, `pragma.r-uu.com` | externer DNS-/Domain-Anbieter |
| Hetzner Cloud Server `r-uu-01-ubuntu` | Host für Caddy, später DMS/Paperless und Pragma | Hetzner-Rechenzentrum |

Zusätzlich spielt die Fritzbox eine Nebenrolle:

| System | Zweck | Wo es läuft |
|---|---|---|
| Fritzbox | stellt deinen Internetzugang bereit; für dieses Hosting keine Portfreigaben nötig | bei dir zuhause |

### 1.2 Visualisierung des Gesamtaufbaus

```text
Browser / SSH-Client
auf deinem Rechner
        |
        | 1. SSH zu Hetzner
        | 2. HTTPS-Aufruf von dms.r-uu.com / pragma.r-uu.com
        v
     Fritzbox / Internetzugang
        |
        v
   Internet / DNS-Aufloesung
        |
        +--> Spaceship DNS
        |    - r-uu.com
        |    - dms.r-uu.com  -> Hetzner-IP
        |    - pragma.r-uu.com -> Hetzner-IP
        |
        v
   Hetzner Cloud Server r-uu-01-ubuntu
        |
        +--> SSH-Server
        |
        +--> Docker
              |
              +--> Caddy
              |     - nimmt HTTP/HTTPS an
              |     - besorgt TLS-Zertifikate
              |     - leitet Requests intern weiter
              |
              +--> DMS / Paperless-Container
              |
              +--> Pragma-Container
```

### 1.3 Erlaeuterung des Zusammenspiels

- **Spaceship** kennt nur Namen und Zieladressen. Dort wird nicht gehostet.
- **Hetzner** stellt den eigentlichen Server bereit, auf dem die Software laeuft.
- **Caddy** ist der Eingangspunkt fuer Webzugriffe auf dem Hetzner-Server.
- **Paperless/DMS** und **Pragma** laufen spaeter als getrennte Container, aber auf demselben Server.
- **Die Fritzbox** ist nur dein Zugang ins Internet. Solange alles bei Hetzner laeuft, brauchst du dort keine Portfreigaben.

### 1.4 Was wir bisher gemacht haben

1. Bei **Spaceship** wurden die DNS-Namen `dms.r-uu.com` und `pragma.r-uu.com` angelegt.
2. Bei **Hetzner** wurde der Server `r-uu-01-ubuntu` erstellt und mit Firewall sowie IPv4 versehen.
3. Per **SSH-Key** wurde ein sicherer Login ohne Passwort eingerichtet.
4. Auf dem Server wurden **Docker** und **Docker Compose** installiert.
5. **Caddy** wurde als Reverse Proxy eingerichtet und hat bereits TLS-Zertifikate erhalten.
6. Danach wurden erste Test-Container gestartet, damit `dms.r-uu.com` und `pragma.r-uu.com` technisch erreichbar sind.

### 1.5 Proxy, Reverse Proxy und Zertifikate

Ein **Proxy** ist allgemein ein Vermittler zwischen zwei Seiten. Er nimmt Anfragen an und leitet sie weiter.

Ein **Reverse Proxy** steht vor deinen internen Diensten. Von aussen sieht man nur ihn; er verteilt die Anfragen dann an die richtigen Container.

Warum das hier sinnvoll ist:

- du hast nur **eine** oeffentliche Maschine
- aber **mehrere** Dienste
- Caddy nimmt HTTP/HTTPS an und entscheidet anhand des Hostnamens, wohin die Anfrage gehen soll

In diesem Setup laeuft es so:

- `dms.r-uu.com` -> Caddy -> Paperless/DMS
- `pragma.r-uu.com` -> Caddy -> Pragma

Die **TLS-Zertifikate** sind die digitalen Ausweise fuer HTTPS.

Wofuer sie da sind:

- sie verschluesseln die Verbindung
- sie bestaetigen dem Browser, dass er wirklich den richtigen Server erreicht
- sie verhindern Warnungen wie "unsicher" oder "Verbindung nicht privat"

Worauf man achten muss:

- die Domain muss per DNS auf den Server zeigen
- der Server muss von aussen auf Port 80/443 erreichbar sein
- Caddy muss den passenden Hostnamen kennen
- jeder Hostname braucht eine eigene, eindeutige Caddy-Konfiguration
- Veraenderungen an den DNS-Eintraegen koennen kurz dauern, bis sie weltweit sichtbar sind

### 1.6 Firewall, Reverse Proxy und SSH-Key zusammen

```text
SSH-Login
dein Rechner
   |
   | SSH-Key prueft dich gegen den Server
   v
Hetzner-Firewall
   |
   | erlaubt nur Port 22 fuer SSH
   v
SSH-Dienst auf dem Server

Webaufruf
dein Browser
   |
   | HTTPS mit TLS-Zertifikat
   v
Hetzner-Firewall
   |
   | erlaubt nur Port 80/443 fuer Web
   v
Caddy (Reverse Proxy)
   |
   | schaut auf den Hostnamen
   +--> dms.r-uu.com  -> Paperless/DMS-Container
   |
   +--> pragma.r-uu.com -> Pragma-Container
```

So greifen die Teile bei dir ineinander:

- **SSH-Key**: du meldest dich ohne Passwort am Server an.
- **Firewall**: sie laesst nur die benoetigten Ports durch.
- **Reverse Proxy Caddy**: er nimmt Webanfragen an und verteilt sie an den richtigen Container.
- **TLS-Zertifikat**: der Browser bekommt HTTPS ohne Warnung und weiss, dass er mit deiner Domain spricht.

In deiner Umsetzung bedeutet das konkret:

- von aussen darf nur das hinein, was du wirklich brauchst
- SSH kommt ueber Port 22 und nur mit Key
- Webseiten kommen ueber Port 80/443
- Caddy sorgt dafuer, dass `dms.r-uu.com` und `pragma.r-uu.com` zur richtigen Anwendung gehen

## 2. Was wurde wo konfiguriert und warum

| Bereich | Konfiguration | Sinn |
|---|---|---|
| Spaceship (DNS) | Subdomains `dms` und `pragma` angelegt | Öffentliche Namen für beide Dienste |
| Hetzner Server | Server `r-uu-01-ubuntu` erstellt | Laufzeitumgebung |
| Hetzner Firewall | Inbound: 22 (SSH), 80 (HTTP), 443 (HTTPS), optional ICMP | Nur notwendige Zugriffe von außen |
| Hetzner Primary IP | Neue IP nur bei ausgeschaltetem Server zuweisbar | IPv4-zuordnung für stabilen SSH-Zugang |
| SSH-Host-Key | Beim ersten Login mit `yes` bestätigt | Vertrauensaufbau und Schutz gegen MITM |
| Fritzbox | Keine Portfreigabe nötig für ausgehende SSH-Verbindung | Server läuft extern bei Hetzner |

## 3. Wichtige Begriffe und Adressen

- **IPv6-Präfix** wie `2a01:4f9:c015:1b06::/64` ist ein Netzbereich, **keine einzelne Zieladresse**.
- Für SSH braucht man eine konkrete Host-Adresse (z. B. `2a01:4f9:c015:1b06::1`) oder eine zugewiesene öffentliche IPv4.
- Servername: `r-uu-01-ubuntu`
- Domain: `r-uu.com`
- Subdomains: `dms.r-uu.com`, `pragma.r-uu.com`

## 4. Häufige Abläufe

### 4.1 SSH-Login

```bash
ssh root@<SERVER-IP>
```

Bevorzugt jetzt per IPv4:

```bash
ssh root@62.238.107.124
```

Für den Hetzner-Server mit IPv6:

```bash
ssh root@2a01:4f9:c015:1b06::1
```

`r-uu-01-ubuntu` funktioniert als SSH-Zielname nur dann, wenn dieser Name auf deinem Rechner auflösbar ist (DNS oder `/etc/hosts`).
Standardmäßig ist der Hetzner-Servername **kein** global auflösbarer DNS-Name. Für den Alltag daher besser:

- öffentliche IP direkt nutzen, oder
- einen eigenen DNS-Namen wie `ssh.r-uu.com` auf die Server-IP zeigen lassen.

Wenn du dich von einem anderen Rechner einloggen willst, brauchst du dort ebenfalls einen passenden SSH-Key. Der Public Key dieses Rechners muss auf dem Server in `~/.ssh/authorized_keys` bzw. `/root/.ssh/authorized_keys` stehen.

### 4.1.1 Alias-Umgebungen sauber trennen

Es gibt jetzt bewusst zwei Alias-Orte:

| Datei | Umgebung | Zweck |
|---|---|---|
| `env/wsl/aliases.sh` | lokale WSL | lokale Repo-Arbeit und SSH-Login nach Hetzner |
| `env/hetzner/aliases.sh` | Hetzner-Server | Docker-/Caddy-/Paperless-Befehle auf dem Server |

Wichtig:

- **WSL** soll nur lokale Befehle enthalten
- **Paperless/Caddy/Docker-Adminbefehle** gehoeren auf den Hetzner-Server

Aktuell bleibt in WSL nur noch der sinnvolle Remote-Helfer:

```bash
ruu-ssh-hetzner
```

Die Hetzner-Aliase werden so auf dem Server aktiviert:

```bash
scp /home/r-uu/develop/github/java/env/hetzner/aliases.sh root@62.238.107.124:/root/.ruu-hetzner-aliases.sh
ssh root@62.238.107.124
grep -qxF 'source /root/.ruu-hetzner-aliases.sh' ~/.bashrc || echo 'source /root/.ruu-hetzner-aliases.sh' >> ~/.bashrc
source /root/.ruu-hetzner-aliases.sh
```

Danach stehen auf Hetzner z. B. diese Befehle bereit:

```bash
ruu-cd-caddy
ruu-caddy-logs --tail=50 caddy
ruu-paperless-create-superuser
ruu-paperless-changepassword <username>
```

Beim ersten Login:

```text
Are you sure you want to continue connecting (yes/no/[fingerprint])?
```

Mit `yes` bestätigen (nach Fingerprint-Prüfung, falls gewünscht).

### 4.2 Host-Key prüfen/aufräumen (lokal)

Die vorhandenen Dateien in `~/.ssh` nicht löschen, da sie für andere Verbindungen wie z. B. zu GitHub die benötigten Daten wie z. B. Schlüssel enthalten. Anstatt löschen nur den Eintrag für den Server entfernen, falls der Server neu aufgesetzt wurde und der Host-Key sich geändert hat.

```bash
ssh-keygen -F <SERVER-IP>
ssh-keygen -R <SERVER-IP>
```

- `-F`: zeigt gespeicherte Host-Key-Einträge
- `-R`: entfernt alten Eintrag (z. B. nach Server-Neuaufbau)

### 4.3 DNS prüfen

```bash
dig +short dms.r-uu.com A
dig +short dms.r-uu.com AAAA
dig +short pragma.r-uu.com A
dig +short pragma.r-uu.com AAAA
```

### 4.4 SSH reagiert nach Inaktivität nicht mehr

Wenn die Verbindung nur eingeschlafen oder abgebrochen ist, hilft normalerweise:

1. die SSH-Sitzung schließen,
2. neu verbinden,
3. den letzten Befehl erneut ausführen.

Für längere Arbeiten auf dem Server ist `tmux` sinnvoll:

```bash
apt -y install tmux
tmux
```

Dann laufen Befehle weiter, auch wenn deine SSH-Verbindung kurz weg ist. Danach kann man mit:

```bash
tmux attach
```

wieder in die Sitzung zurück.

### 4.5 Root-Passwort und SSH-Passwort-Login

Für `root` wurde ein Passwort gesetzt. Das ist sinnvoll als Notfallzugang für Konsole/Recovery, sollte aber nicht den normalen SSH-Key-Login ersetzen.

Wenn dieser Befehl keine Ausgabe liefert:

```bash
grep -E '^(PasswordAuthentication|PermitRootLogin)' /etc/ssh/sshd_config /etc/ssh/sshd_config.d/*.conf 2>/dev/null
```

dann sind diese Werte meist nicht explizit gesetzt. Entscheidend ist daher die effektive SSH-Konfiguration:

```bash
sshd -T | grep -E 'passwordauthentication|permitrootlogin'
```

Sichere Zielwerte:

- `passwordauthentication no`
- `permitrootlogin prohibit-password` oder `permitrootlogin no`

Zwischenstand waehrend der Einrichtung

- `permitrootlogin prohibit-password`
- `passwordauthentication yes`

bedeutet:

- `root` darf sich **nicht** per Passwort per SSH anmelden
- Passwort-Login ist für andere SSH-Benutzer grundsätzlich noch aktiviert

Wenn nur SSH-Keys verwendet werden sollen, ist `passwordauthentication no` die bessere Einstellung.

Falls die Werte nicht passen, kann man sie explizit setzen:

```bash
printf '%s\n' 'PasswordAuthentication no' 'PermitRootLogin prohibit-password' > /etc/ssh/sshd_config.d/99-hardening.conf
sshd -t && systemctl reload ssh
```

Aktueller Soll- und Ist-Zustand:

```bash
sshd -T | grep -E 'passwordauthentication|permitrootlogin'
permitrootlogin prohibit-password
passwordauthentication no
```

Damit ist SSH jetzt so gehärtet, dass `root` nur noch per SSH-Key anmeldbar ist.

## 5. Nächster Schritt: Basis-Setup auf dem Hetzner-Server

Die hier aufgeführten Kommandos sind bereits erfolgreich durchgelaufen.

Auf dem Server als `root` ausführen:

```bash
apt update && apt -y upgrade
apt -y install ca-certificates curl gnupg ufw fail2ban

# Docker Repository vorbereiten
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg
. /etc/os-release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $VERSION_CODENAME stable" > /etc/apt/sources.list.d/docker.list

apt update
apt -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

Danach folgt:

1. Prüfen, ob Docker und Docker Compose verfügbar sind
   - `docker --version`
   - `docker compose version`
2. Einen Reverse Proxy einrichten (empfohlen: Caddy)
3. Ein Verzeichnis für die Deployments anlegen, z. B. `/opt/stacks`
4. Paperless/DMS und Pragma per `docker compose` deployen
5. HTTPS und Routing über `dms.r-uu.com` und `pragma.r-uu.com` prüfen

## 6. Caddy als Reverse Proxy einrichten

Caddy ist für den Start sinnvoll, weil es HTTPS automatisch verwaltet und die Konfiguration sehr kurz hält.

### 6.1 Verzeichnis anlegen

```bash
mkdir -p /opt/stacks/caddy
cd /opt/stacks/caddy
```

### 6.2 `docker-compose.yml` anlegen

```yaml
services:
  caddy:
    image: caddy:2
    container_name: caddy
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile:ro
      - caddy_data:/data
      - caddy_config:/config

volumes:
  caddy_data:
  caddy_config:
```

### 6.3 `Caddyfile` anlegen

Erstmal nur ein einfacher Test, damit Caddy sauber startet. Wichtig: pro Hostname darf es nur **einen** Block geben. Kommentare beginnen mit `#`.

```caddyfile
# Testkonfiguration
dms.r-uu.com {
  respond "Caddy läuft"
}

pragma.r-uu.com {
  respond "Caddy läuft"
}
```

Wenn du später auf echte Dienste umstellst, ersetzt du diese Blöcke durch `reverse_proxy`, aber nicht zusätzlich mit alten Test-Blöcken kombinieren.

Typischer Fehler dabei:

- derselbe Hostname kommt zweimal vor
- Caddy meldet dann `ambiguous site definition`
- dann immer die alten Test-Bloecke entfernen und nur die neue Variante stehen lassen

### 6.4 Starten

```bash
docker compose up -d
docker compose logs -f
```

Wenn alles passt, sollten die Subdomains später per Browser HTTPS liefern. Danach wird das `respond` durch `reverse_proxy` auf die eigentlichen Dienste ersetzt.

### 6.5 Aktueller Status

Bereits erfolgreich erreicht:

- Caddy-Container läuft
- Für `dms.r-uu.com` und `pragma.r-uu.com` wurden Let's-Encrypt-Zertifikate erfolgreich ausgestellt
- Automatische HTTPS-Verwaltung ist aktiv

Nützliche Befehle:

```bash
cd /opt/stacks/caddy
docker compose ps
docker compose logs --tail=100 caddy
```

`docker compose logs -f` bleibt offen. Mit `Ctrl + C` verlässt man nur die Log-Anzeige, der Container läuft weiter.

### 6.6 Gemeinsames Docker-Netz für Reverse Proxy

Damit Caddy später Container aus anderen Compose-Stacks per Namen erreichen kann, sollten alle betroffenen Stacks dasselbe externe Docker-Netz nutzen.

Netz einmalig anlegen:

```bash
docker network create proxy
```

Dann `docker-compose.yml` von Caddy anpassen:

```yaml
services:
  caddy:
    image: caddy:2
    container_name: caddy
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile:ro
      - caddy_data:/data
      - caddy_config:/config
    networks:
      - proxy

volumes:
  caddy_data:
  caddy_config:

networks:
  proxy:
    external: true
```

Danach neu laden:

```bash
cd /opt/stacks/caddy
docker compose up -d
docker network inspect proxy
```

Dieses Netz muss später auch in den Compose-Dateien von `dms` und `pragma` eingetragen werden.

### 6.7 Reihenfolge der nächsten Schritte

Ab hier ist die sinnvolle Reihenfolge:

1. Gemeinsames Docker-Netz `proxy` anlegen
2. Caddy an dieses Netz hängen
3. Im Browser prüfen, dass `https://dms.r-uu.com` und `https://pragma.r-uu.com` bereits von Caddy beantwortet werden
4. Einen ersten echten Anwendungs-Stack anlegen
5. Das `respond` im `Caddyfile` später durch `reverse_proxy` ersetzen

Empfehlung: zuerst die Netzbasis sauber fertigstellen, danach **einen** Dienst vollständig anbinden, nicht beide parallel.

## 7. Naechste Schritte fuer Paperless

Der Reverse Proxy funktioniert bereits, und `dms.r-uu.com` zeigt schon auf Paperless. Jetzt geht es nicht mehr um Server und Netzwerk, sondern um die eigentliche Nutzung von Paperless als persoenliches Dokumentenarchiv.

### 7.1 Ziel fuer deine Nutzung

Dein sinnvoller Zielzustand ist:

- du kannst unter **Windows** eingescannte Dokumente in Paperless uebernehmen
- du kannst mit dem **Handy fotografierte Dokumente** ebenfalls sauber importieren
- Paperless fuehrt danach moeglichst viel automatisch aus:
  - OCR / Texterkennung
  - Vorschaubild
  - Volltextsuche
  - Ablage mit Metadaten wie Dokumenttyp, Tags und Korrespondent

Wichtig: fuer gute OCR ist ein **sauberes PDF** fast immer besser als ein rohes Handyfoto. Papierdokumente sollten daher idealerweise:

- vom Scanner direkt als **PDF**
- vom Handy moeglichst ueber eine **Scan-Funktion mit Zuschneiden und Kontrastkorrektur** ebenfalls als **PDF**

hochgeladen werden. Ein normales JPG-Foto funktioniert oft auch, ist aber meist schlechter fuer OCR und Lesbarkeit.

### 7.2 Unmittelbar noetige Ersteinrichtung

1. **Superuser anlegen**
   ```bash
   docker exec -it dms paperless-manage createsuperuser
   ```
   Falls das Image nur `python3` hat:
   ```bash
   docker exec -it dms python3 /app/paperless/src/manage.py createsuperuser
   ```
2. **Im Browser anmelden** unter `https://dms.r-uu.com`
3. **Ein Testdokument hochladen**
4. **Pruefen**, ob OCR und Suche grundsaetzlich funktionieren

Fuer den allerersten Start musst du in Paperless noch nicht viel konfigurieren. Wichtig ist zuerst nur:

- Login funktioniert
- Upload funktioniert
- das Dokument erscheint in der Liste
- der erkannte Text ist suchbar

### 7.3 Was du innerhalb von Paperless sinnvoll einrichten solltest

Fuer deine eher private und moderate Nutzung ist ein einfacher Aufbau besser als ein ueberkomplexes Regelwerk. Am Anfang reichen diese Bausteine:

#### 7.3.1 Dokumenttypen

Dokumenttypen helfen dir, Dokumente fachlich einzuordnen. Sinnvolle Startwerte koennen sein:

- Rechnung
- Vertrag
- Brief
- Versicherung
- Bank
- Gesundheit
- Steuer
- Anleitung
- Garantie
- Sonstiges

Der Dokumenttyp ist spaeter sehr hilfreich beim Filtern und Wiederfinden.

#### 7.3.2 Tags

Tags sind flexibler als Dokumenttypen. Sie eignen sich gut fuer zusaetzliche Merkmale wie:

- privat
- wichtig
- offen
- erledigt
- original
- scan
- handy

Empfehlung: nicht zu viele Tags auf einmal anlegen. Lieber mit wenigen, klaren Begriffen starten.

#### 7.3.3 Korrespondenten

Korrespondenten sind Absender oder Gegenstellen, zum Beispiel:

- Bank
- Versicherung
- Vermieter
- Arbeitgeber
- Arztpraxis
- Stadtverwaltung

Du musst nicht sofort eine grosse Liste pflegen. Lege Eintraege nur dann an, wenn sie dir beim Wiederfinden wirklich helfen.

#### 7.3.4 Speicherpfade

Speicherpfade sind optional, aber spaeter sehr praktisch. Damit kann Paperless Dokumente logisch ablegen, zum Beispiel nach:

- Jahr
- Dokumenttyp
- Korrespondent

Fuer den Start kannst du diesen Punkt auch zunaechst offenlassen. Wichtiger sind zuerst Upload, OCR und Suche.

#### 7.3.5 Abbildung deiner Thunderbird-Ordnerstruktur in Paperless

Deine bestehende Thunderbird-Struktur ist als fachliche Orientierung sehr gut. In Paperless sollte sie aber nicht 1:1 als tiefer Ordnerbaum nachgebaut werden, sondern als Kombination aus:

- **Dokumenttyp** fuer die Hauptkategorie
- **Korrespondent** fuer Absender / Vertragspartner
- **Tags** fuer Sonderfaelle und Quermerkmale

Empfohlene Zuordnung fuer deine Hauptordner:

- `01-invoice` -> Dokumenttyp `invoice`
- `02-purchase` -> Dokumenttyp `purchase`
- `03-contract` -> Dokumenttyp `contract`
- `04-bank` -> Dokumenttyp `bank`
- `05-tax` -> Dokumenttyp `tax`
- `06-work` -> Dokumenttyp `work`
- `07-develop` -> Dokumenttyp `develop`
- `08-private` -> Dokumenttyp `private`

Unterordner wie `c24`, `sparkasse`, `paypal`, `hetzner`, `physicians`, `nusser` passen besser als **Korrespondenten**.

Sonderordner nicht als Dokumenttyp, sondern als Tag:

- `98-suspected trash` -> Tag `review` oder `trash-candidate`
- `99-uncategorised` -> Tag `uncategorised`

Warum diese Abbildung sinnvoll ist:

- weniger Pflegeaufwand als bei tiefen Ordnerbaeumen
- deutlich staerkere Suche und Filterung in Paperless
- spaeter leichter automatisierbar ueber IMAP-Regeln, Korrespondenten und Tags

### 7.4 Empfohlener Start-Workflow fuer dich

Fuer deine Nutzung ist dieser Start am sinnvollsten:

1. zuerst **manueller Upload ueber die Weboberflaeche**
2. danach bei Bedarf ein **automatischer Eingangskanal**

Der Grund ist einfach:

- der manuelle Upload ist sofort verstaendlich
- du lernst die Paperless-Oberflaeche kennen
- Fehlerquellen bleiben am Anfang klein
- spaetere Automatisierung baut auf einem bereits funktionierenden Grundablauf auf

### 7.5 So arbeitest du mit Windows-Scans

Der einfachste Weg ist:

1. auf Windows mit dem Scanner als **PDF** scannen
2. die Datei lokal speichern
3. in `https://dms.r-uu.com` anmelden
4. das PDF per Drag-and-drop oder Upload in Paperless hochladen
5. danach Dokumenttyp, Tags und ggf. Korrespondent setzen

Das ist fuer den Anfang ideal, weil:

- kein weiterer Sync-Dienst noetig ist
- du den Import sofort kontrollieren kannst
- du direkt siehst, ob OCR und Dateiqualitaet gut genug sind

Wenn du spaeter mehr Komfort willst, ist als naechste Ausbaustufe sinnvoll:

- ein fester **Windows-Scan-Ordner**
- automatische Uebertragung dieses Ordners auf den Server
- Import in den **Paperless-Consume-Ordner**

Das ist aber ein separater Automatisierungsschritt und fuer den Start nicht noetig.

### 7.6 So arbeitest du mit Handy-Fotos

Empfohlen ist nicht das einfache Kamerafoto, sondern eine Scan-Funktion auf dem Handy, die:

- Dokumentrander erkennt
- perspektivisch begradigt
- Kontrast verbessert
- am Ende ein PDF erzeugt

Dann ist der Ablauf:

1. Dokument mit dem Handy als Scan-PDF erfassen
2. PDF lokal auf dem Handy speichern oder teilen
3. in Paperless per mobilem Browser hochladen
4. danach kurz Metadaten pruefen

Warum dieser Weg gut ist:

- bessere OCR-Ergebnisse
- weniger schiefe oder dunkle Bilder
- angenehmere Archivansicht

Ein direktes JPG-Foto kannst du zwar ebenfalls hochladen, aber es ist eher die Ausweichloesung als der Idealweg.

### 7.7 Welche Paperless-Einstellungen fuer den Anfang wirklich wichtig sind

Innerhalb von Paperless solltest du zuerst vor allem auf diese Punkte achten:

1. **Benutzer und Passwort**
   - Admin-Zugang anlegen
   - spaeter bei Bedarf einen normalen Benutzer anlegen
2. **Sprache, Zeitzone, persoenliche Anzeigeoptionen**
   - falls in der Oberflaeche verfuegbar, an deine Nutzung anpassen
3. **Dokumenttypen**
   - kleine, brauchbare Startmenge anlegen
4. **Tags**
   - wenige alltagstaugliche Tags anlegen
5. **Korrespondenten**
   - nur die wichtigsten anlegen

Noch nicht sofort noetig sind:

- komplexe automatische Regeln
- umfangreiche Mail-Importe
- aufwendige Ablagestrukturen
- sehr feingranulare Tag-Systeme

### 7.8 Woran du erkennst, dass Paperless fuer dich korrekt arbeitet

Nach einem erfolgreichen Test solltest du Folgendes sehen:

- das Dokument erscheint in der Dokumentliste
- eine Vorschau ist sichtbar
- der erkannte Text ist durchsuchbar
- Titel und Datum koennen angepasst werden
- Dokumenttyp und Tags lassen sich speichern

Wenn das funktioniert, ist Paperless fuer den Alltagsbetrieb im Kern einsatzbereit.

### 7.9 Empfohlene Reihenfolge fuer die naechsten Schritte

1. Superuser anlegen
2. erfolgreich im Browser anmelden
3. ein Scanner-PDF von Windows hochladen
4. ein mit dem Handy als Scan-PDF erzeugtes Dokument hochladen
5. pruefen, ob Suche und OCR bei beiden sauber funktionieren
6. erst danach entscheiden, ob du einen automatischen Consume-Ordner einrichten willst

### 7.10 Wichtiger Hinweis fuer spaetere Automatisierung

Wenn du spaeter Dokumente nicht mehr manuell hochladen willst, ist der naechste sinnvolle Schritt nicht "mehr in Paperless klicken", sondern ausserhalb von Paperless eine geregelte Zufuhr einzurichten, zum Beispiel:

- Windows scannt in einen festen Eingangsordner
- das Handy liefert in einen festen Upload- oder Sync-Ordner
- dieser Ordner wird auf den Server uebertragen
- Paperless importiert die Dateien ueber seinen Consume-Mechanismus

Das ist komfortabler, aber auch ein eigener Einrichtungsblock. Fuer jetzt ist der manuelle Web-Upload die beste, einfachste und verstaendlichste Startloesung.

## 8. Workflows für paperless



### 8.1 Mails

- Primärer mail account ist und bleibt aus historischen Gründen web.de.
- web.de forwardet alle Mails an gmx.de und mail.de.
  - gmx.de hat keine gute Ordnerstruktur, aber einen paperless-ordner, der vom paperless-server für die Abholung von Mails erreicht werden kann.
  - mail.de hat eine gute Ordnerstruktur, kann aber derzeit vom paperless-server nicht direkt für die Abholung von Mails erreicht werden.
- thunderbird ist der Mailclient, der mit Filtern die Mails von mail.de
  - in die imap-Ordnerstruktur von mail.de und
  - in den imap-paperless-ordner von gmx.de verteilt.
- paperless holt die Mails aus dem imap-paperless-ordner von gmx.de ab und verarbeitet sie.

### 8.2 Scanner

### 8.3 Handy

### 8.4 Dateien

## 9. Was an der Fritzbox normalerweise nötig ist

Für dieses Hosting-Szenario meist **nichts**:

- Kein Port-Forwarding für den Hetzner-Server notwendig
- Keine DynDNS-Anpassung für den Hetzner-Server erforderlich

Nur wenn du später Dienste aus deinem Heimnetz veröffentlichen willst, werden Portfreigaben/DynDNS in der Fritzbox relevant.
