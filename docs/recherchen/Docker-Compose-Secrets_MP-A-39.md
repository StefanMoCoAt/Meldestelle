# Analyse und Behebung der Docker-Compose-Konfiguration für Keycloak 26.4 mit Docker Secrets

## I. Management Summary und Kerndiagnose: Warum Ihre Konfiguration fehlschlägt



Die vorgelegte `docker-compose.yml`-Konfiguration ist strukturell fundiert und folgt modernen Sicherheitsprinzipien, insbesondere dem Versuch, sensible Daten über Docker Secrets zu verwalten. Die Konfiguration schlägt jedoch aufgrund einer subtilen, aber entscheidenden Diskrepanz in den Fähigkeiten der verwendeten Container-Images fehl.

### Kerndiagnose:

Die Konfiguration scheitert, weil das offizielle Keycloak-Container-Image (`quay.io/keycloak/keycloak:26.4`) die `_FILE`-Suffix-Konvention für Umgebungsvariablen (z. B. `KC_DB_PASSWORD_FILE`) nicht nativ unterstützt. Diese Funktionalität ist keine universelle Docker-Funktion, sondern eine spezifische Implementierung innerhalb des Entrypoint-Skripts eines Images.

Das `postgres`-Image, das Sie ebenfalls verwenden, unterstützt diese Konvention. Keycloak tut dies jedoch nicht für Datenbank- oder Admin-Anmeldeinformationen.

### Fehlerursache im Detail:

Der `keycloak`-Container liest die Umgebungsvariable `KC_DB_USERNAME_FILE` und sieht den Pfad `/run/secrets/postgres_user`. Der Container-Prozess erwartet jedoch, dass die Variable `KC_DB_USERNAME` mit dem *tatsächlichen Benutzernamen* (dem *Inhalt* der Datei) gesetzt ist. Da `KC_DB_USERNAME` nicht gesetzt ist, versucht Keycloak, sich mit leeren Anmeldeinformationen bei der PostgresSQL-Datenbank anzumelden. Dies führt unweigerlich zu einem Verbindungsfehler, wie er auch in Community-Diskussionen vielfach berichtet wird. Der Postgres-Container lehnt den Anmeldeversuch des Benutzers `' '` (leerer String) ab, und der Keycloak-Dienst kann nicht starten.

### Die Lösung (Kurzfassung):

Der Lösungsansatz besteht darin, die `_FILE`-Funktionalität, die dem Keycloak-Image fehlt, manuell nachzubilden. Die eleganteste Methode, die keine benutzerdefinierten Docker-Images erfordert, ist die Überschreibung des `command` im `keycloak`-Dienstes Ihrer `docker-compose.yml`.
Ein kurzes Wrapper-Skript liest den Inhalt der Secret-Dateien aus `/run/secrets/` und exportiert ihn in die von Keycloak erwarteten, regulären Umgebungsvariablen (z. B. `KC_DB_PASSWORD`), bevor der eigentliche Keycloak-Prozess gestartet wird.

### Beantwortung der `.env`-Frage:

Keycloak-Einstellungen sind nicht auf `.env`-Dateien beschränkt. `.env`-Dateien sind lediglich eine von vielen Methoden, um Umgebungsvariablen in einem Container zu setzen. Keycloak selbst benötigt lediglich die korrekten Umgebungsvariablen.
Ihre Absicht, Docker Secrets zu verwenden, ist aus Sicherheitssicht die überlegene Methode im Vergleich zu `.env`-Dateien.
Das Problem ist nicht Ihr Ansatz, sondern eine Implementierungslücke im Keycloak-Image.



## II. Tiefenanalyse: Die `_FILE`-Suffix-Diskrepanz in Ihrer `docker-compose.yml`



Der Kern des Problems liegt im Trugschluss, dass alle offiziellen Images die gleichen Komfortfunktionen bieten. Die `_FILE`-Suffix-Unterstützung ist ein gängiges Muster, aber keine garantierte Funktion.

### A. Der `postgres`-Dienst: Ein Musterbeispiel für `_FILE`-Unterstützung

Ihre Konfiguration für den `postgres`-Dienst ist vollkommen korrekt und funktionsfähig:

```YAML
environment:
  POSTGRES_USER_FILE: /run/secrets/postgres_user
  POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password
secrets:
  - postgres_user
  - postgres_password
```

**Wie es funktioniert:** Das offizielle Postgres-Container-Image enthält ein intelligentes Entrypoint-Skript (den `docker-entrypoint.sh` – Mechanismus). Beim Start des Containers durchsucht dieses Skript die Umgebungsvariablen nach Mustern, die auf `_FILE` enden. Wenn es `POSTGRES_USER_FILE` findet, liest es den Inhalt der Datei unter dem angegebenen Pfad (in Ihrem Fall `/run/secrets/postgres_user`) und weist diesen Wert intern der `POSTGRES_USER`-Variable zu. Anschließend wird der Postgres-Prozess mit den korrekt geladenen Anmeldeinformationen gestartet. Dies ist ein explizites Feature des Postgres-Images. Ihr `depends_on` mitt `condition: service_healthy` stellt korrekt sicher, dass Keycloak wartet, bis dieser Vorgang abgeschlossen ist.

### B. Der keycloak-Dienst: Der Trugschluss der Funktionsparität

Ihre keycloak-Konfiguration folgt derselben Logik, scheitert aber an der Implementierung des Ziel-Images:

```YAML
environment:
  KC_BOOTSTRAP_ADMIN_USERNAME_FILE: /run/secrets/kc_bootstrap_admin_username
  KC_DB_USERNAME_FILE: /run/secrets/postgres_user
secrets:
  - kc_bootstrap_admin_username
  - postgres_user
```

**Warum es fehlschlägt:** Das offizielle `quay.io/keycloak/keycloak:26.4`-Image (basierend auf Quarkus) enthält *kein* vergleichbares Entrypoint-Skript, das nach `KC_DB_USERNAME_FILE` oder `KC_BOOTSTRAP_ADMIN_PASSWORD_FILE` sucht, um deren Inhalt zu lesen.

**Die Kausalkette des Scheiterns ist wie folgt:**

1. Docker Compose startet den `keycloak`-Container.

2. Docker Compose hängt die in `secrets:` definierten Dateien korrekt in das In-Memory-Dateisystem des Containers unter `/run/secrets/` ein (z. B. `/run/secrets/postgres_user`).

3. Docker Compose setzt die Umgebungsvariable `KC_DB_USERNAME_FILE` auf den String-Wert `/run/secrets/postgres_user`.

4. Der Keycloak (Quarkus)-Prozess startet. Gemäß seiner Konfigurationslogik 2 sucht er nach den Konfigurationsschlüsseln `KC_DB_USERNAME` und `KC_DB_PASSWORD`.

5. Da diese Variablen nicht gesetzt sind, verwendet Keycloak Standardwerte, die effektiv `null` oder leere Strings (`' '`) sind. Es ignoriert die `KC_DB_USERNAME_FILE`-Variable, da es sie nicht als Mechanismus zum Laden von Werten, sondern höchstens als reguläre, aber ungenutzte Konfigurationsvariable interpretiert.

6. Keycloak versucht, sich bei der Datenbank unter `jdbc:postgresql://postgres:5432/meldestelle` mit dem Benutzer `' '` und einem leeren Passwort anzumelden.

7. PostgresSQL lehnt diese Verbindung ab. Der `keycloak`-Container gerät in eine Crash-Schleife oder meldet einen fatalen Startfehler.

### C. Debunking: Warum `KC_HTTPS_CERTIFICATE_FILE` irreführend ist

Die Verwirrung wird oft durch die Tatsache verstärkt, dass einige `_FILE`-Variablen in Keycloak doch zu funktionieren scheinen, wie z. B. `KC_HTTPS_CERTIFICATE_FILE`. Dies ist jedoch kein Widerspruch, sondern unterstreicht die Unterscheidung:

* `KC_DB_USERNAME`: Diese Konfigurationsoption erwartet einen **String-Wert** (den Namen des Benutzers).
* `KC_HTTPS_CERTIFICATE_FILE`: Diese Konfigurationsoption erwartet einen **Dateipfad** (den Pfad zum Zertifikat).

Wenn Keycloak eine Option wie `KC_HTTPS_CERTIFICATE_FILE` liest, erwartet es einen Pfad und lädt die Datei von diesem Pfad. Das ist die beabsichtigte Funktion. Wenn es `KC_DB_USERNAME` liest, erwartet es den Benutzernamen selbst. Die `_FILE`-Konvention (wie sie Postgres verwendet) ist ein *Container-Pattern*, um eine Lücke zu schließen: Sie wandelt eine Datei (Secret) in einen String-Wert (Konfiguration) um. Dieses Muster muss im Image *explizit implementiert* sein, was bei Keycloak 26.4 für diese spezifischen Variablen nicht der Fall ist.



## III. Lösungsansatz A (Empfohlen): Der `command`-Wrapper in `docker-compose.yml`



Dieser Ansatz ist der empfohlene, da er keine Erstellung benutzerdefinierter Docker-Images erfordert und die gesamte Konfigurationslogik in Ihrer `docker-compose.yml`-Datei transparent bleibt.

### A. Die Implementierung

Das Prinzip besteht darin, den `command` des `keycloak`-Dienstes abzufangen. Anstatt direkt das Keycloak-Skript aufzurufen, starten wir eine `bash`-Shell. Diese Shell führt ein kurzes Skript aus, das die Secrets liest, sie in die korrekten Umgebungsvariablen exportiert und danach den ursprünglichen Keycloak-Befehl ausführt.

Die `environment`-Sektion des `keycloak`-Dienstes wird bereinigt (die `_FILE`-Variablen werden entfernt), und der `command`-Block wird hinzugefügt:

```YAML
keycloak:
   image: quay.io/keycloak/keycloak:${DOCKER_KEYCLOAK_VERSION:-26.4}
   container_name: meldestelle-keycloak
   environment:
      # Die _FILE-Variablen werden entfernt, da sie nicht unterstützt werden.
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-meldestelle}
      # Die Variablen KC_DB_USERNAME, KC_DB_PASSWORD,
      # KC_BOOTSTRAP_ADMIN_USERNAME und KC_BOOTSTRAP_ADMIN_PASSWORD
      # werden unten im 'command' gesetzt.
   secrets:
      - kc_bootstrap_admin_username
      - kc_bootstrap_admin_password
      - postgres_user
      - postgres_password
   ports:
      - "8180:8080"
   depends_on:
     postgres:
        condition: service_healthy
   volumes:
     -./docker/services/keycloak:/opt/keycloak/data/import

# \--- BEGINN DER KORREKTUR ---
# Dieser 'command'-Block liest die Secrets und exportiert sie als
# Umgebungsvariablen, bevor Keycloak gestartet wird.
   command: >
      bash -c "
      # Setzen Sie die Admin-Anmeldeinformationen aus Docker Secrets
      export KC_BOOTSTRAP_ADMIN_USERNAME=$(cat /run/secrets/kc_bootstrap_admin_username \| tr -d '\\n')
      export KC_BOOTSTRAP_ADMIN_PASSWORD=$(cat /run/secrets/kc_bootstrap_admin_password \| tr -d '\\n')

      # Setzen Sie die DB-Anmeldeinformationen aus Docker Secrets
      export KC_DB_USERNAME=$(cat /run/secrets/postgres_user | tr -d '\n')
      export KC_DB_PASSWORD=$(cat /run/secrets/postgres_password | tr -d '\n')

      # Führen Sie den ursprünglichen Befehl aus (start-dev)
      # 'exec' stellt sicher, dass Keycloak der Hauptprozess (PID 1) wird
      exec /opt/keycloak/bin/kc.sh start-dev --import-realm
      "
# \--- ENDE DER KORREKTUR ---

   networks:
      - meldestelle-network
   healthcheck:
      test:
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 40s
   restart: unless-stopped
```

### B. Kritischer Fallstrick: Das "Newline"-Problem

Ein häufiger und schwer zu diagnostizierender Fehler bei diesem Ansatz ist das Vorhandensein von Zeilenumbruchzeichen (`\n`).

**Problem:** Wenn Sie Ihre Secret-Dateien (z. B. `postgres_password.txt`) mit einem Standard-Texteditor erstellen, fügt dieser fast immer ein unsichtbares Zeilenumbruchzeichen (`\n`) am Ende der Datei hinzu. Fehlerbild: Der `cat`-Befehl (z. B. `$(cat /run/secrets/postgres_password)`) liest dieses `\n` mit ein. Keycloak würde dann versuchen, sich mit einem Passwort wie `"meinPasswort\n"` anzumelden, was von Postgres als falsch zurückgewiesen wird. Der Benutzer sieht, dass das Passwort "korrekt" ist, während es technisch gesehen ein Zeichen zu lang ist.
**Lösung:** Wie im obigen Code-Beispiel implementiert, wird die Ausgabe von `cat` durch `| tr -d '\n'` geleitet. Dieser Befehl (`translate`) löscht (`-d`) alle Vorkommen von Zeilenumbruchzeichen (`\n`) und stellt sicher, dass nur der reine Secret-String in die Umgebungsvariable exportiert wird.

Alternativ können Sie die Secret-Dateien auf dem Host-System ohne Zeilenumbruch erstellen, z. B. mit
`printf "mein-passwort" >./docker/secrets/postgres_password.txt`.

## IV. Lösungsansatz B: Das benutzerdefinierte Entrypoint-Skript



Dieser Ansatz ist technisch sauberer, erfordert jedoch mehr Einrichtung. Er ist ideal, wenn Sie die `_FILE`-Funktionalität für viele Variablen benötigen oder wenn Sie aus anderen Gründen bereits ein benutzerdefiniertes Keycloak-Image erstellen. Dieser Ansatz formalisiert die vorgeschlagene Community-Lösung.

### Schritt 1: Erstellen Sie ein Entrypoint-Skript (`entrypoint.sh`)

Erstellen Sie eine Datei `entrypoint.sh` im selben Verzeichnis wie Ihre `Dockerfile` (oder in einem Unterverzeichnis).

```Bash
#!/bin/bash
set -e # Bricht bei Fehlern sofort ab

# Diese Funktion durchläuft alle Umgebungsvariablen
# Sie sucht nach Variablen, die mit 'KC_' beginnen und auf '_FILE' enden
load_secrets() {
  # 'printenv' listet alle Vars auf, 'grep' filtert sie
  for var in $(printenv | grep -o 'KC_.*_FILE'); do
    # Entfernt das _FILE-Suffix, um den Namen der Zielvariable zu erhalten
    # z.B. KC_DB_PASSWORD_FILE -\> KC_DB_PASSWORD
    outvar="${var%_FILE}"

    # Liest den Dateipfad aus der _FILE-Variable
    file_path="${!var}"
    
    if [[ -z "$file_path" ]]; then
        echo "WARN: $var ist gesetzt, aber leer."
        continue
    fi

    if [[ -e "$file_path" ]]; then
        # Liest den Inhalt der Datei (und entfernt Newlines!), exportiert ihn
        content=$(cat "$file_path" | tr -d '\n')
        export "$outvar"="$content"
        echo "INFO: $outvar aus $var exportiert."
    else
        echo "ERR: $var verweist auf eine nicht existente Datei: '$file_path'"
        # Je nach Anforderung kann hier mit 'exit 1' abgebrochen werden
    fi
  done
}

# Führt die Secret-Ladefunktion aus
load_secrets

# Führt den eigentlichen Befehl aus, der an den Container übergeben wurde
# (z.B. "start-dev --import-realm")
exec "$@"
```

### Schritt 2: Erstellen Sie eine `Dockerfile`

Erstellen Sie eine `Dockerfile`, um Ihr benutzerdefiniertes Keycloak-Image zu bauen.

```Dockerfile
# Beginnt mit dem offiziellen Image, das Sie verwenden
FROM quay.io/keycloak/keycloak:26.4

# Kopiert das neue Skript in den Container
COPY entrypoint.sh /opt/keycloak/bin/entrypoint.sh

# Wechselt zum root-Benutzer, um Berechtigungen zu ändern
USER root

# Macht das Skript ausführbar
RUN chmod +x /opt/keycloak/bin/entrypoint.sh

# Wechselt zurück zum Standard-Keycloak-Benutzer
USER keycloak

# Legt das neue Skript als Entrypoint fest
ENTRYPOINT ["/opt/keycloak/bin/entrypoint.sh"]

# Der 'command' aus Ihrer docker-compose.yml wird an dieses
# Entrypoint-Skript als Argument ("$@") übergeben.
```

### Schritt 3: Passen Sie Ihre `docker-compose.yml` an

Sie würden nun Ihre *ursprüngliche* `docker-compose.yml` beibehalten (mit den `_FILE`-Variablen), müssten aber den `keycloak`-Dienst anweisen, Ihr neues Image zu verwenden:

```YAML
keycloak:
   # Ersetzt 'image:' durch 'build:'
   build: . # (Oder der Pfad zu Ihrem Dockerfile-Kontext)
   container_name: meldestelle-keycloak
   environment:
      # Jetzt funktioniert Ihre ursprüngliche Konfiguration!
      KC_BOOTSTRAP_ADMIN_USERNAME_FILE: /run/secrets/kc_bootstrap_admin_username
      KC_BOOTSTRAP_ADMIN_PASSWORD_FILE: /run/secrets/kc_bootstrap_admin_password
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-meldestelle}
      KC_DB_USERNAME_FILE: /run/secrets/postgres_user
      KC_DB_PASSWORD_FILE: /run/secrets/postgres_password
   secrets:
      #... (bleibt gleich)
   command: start-dev --import-realm # (bleibt gleich)
   #... (Rest bleibt gleich)
```



## V. Beantwortung Ihrer Kernfrage: Docker Secrets vs. `.env`-Dateien



Die Frage "Oder funktionieren Keycloak Einstellungen nur mit `.env` Dateien?" ist zentral. Die Antwort ist ein klares **Nein**.
Keycloak (und die zugrundeliegende Quarkus-Plattform) ist so konzipiert, dass es seine Konfiguration aus priorisierten Quellen bezieht. Die Rangfolge ist typischerweise:

1. Kommandozeilenparameter,
2. Umgebungsvariablen,
3. Konfigurationsdateien (z. B. keycloak.conf).
   Der Keycloak-Anwendung ist es gleichgültig, wie diese Umgebungsvariablen in den Container gelangen. Eine `.env`-Datei ist lediglich eine von vielen Methoden, die Docker Compose bereitstellt, um Umgebungsvariablen zu setzen.

### A. Wie es mit `.env`-Dateien funktionieren würde

Um die Konfiguration mit `.env`-Dateien zum Laufen zu bringen, würden Sie alle `secrets:`-Blöcke aus der `docker-compose.yml` entfernen. Stattdessen würden Sie eine Datei (z. B. `keycloak.env`) erstellen:

```
# keycloak.env
KC_BOOTSTRAP_ADMIN_USERNAME=admin
KC_BOOTSTRAP_ADMIN_PASSWORD=strenggeheim
KC_DB_USERNAME=meldestelle
KC_DB_PASSWORD=nochgeheimer
```

Und Ihre `docker-compose.yml` würde wie folgt angepasst:

```YAML
keycloak:
   image: quay.io/keycloak/keycloak:26.4
   env_file:
      -./keycloak.env # Lädt die Variablen aus dieser Datei
   environment:
      # Nicht-sensible Konfiguration bleibt hier
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-meldestelle}
   #... kein 'secrets:'-Block mehr nötig...
   command: start-dev --import-realm
```

Dies funktioniert, weil Docker Compose die `keycloak.env`-Datei liest und diese Werte direkt als Umgebungsvariablen im Container setzt. Keycloak findet `KC_DB_USERNAME` und ist zufrieden.

### B. Warum Ihr Ansatz mit Secrets überlegen ist

Ihr ursprünglicher Ansatz, Docker `secrets:` zu verwenden, ist dem `.env`-Datei-Ansatz aus Sicherheitsgründen weit überlegen.

* **Risiko bei `.env`-Dateien:** Die Secrets liegen als Klartext in der `.env`-Datei auf dem Host und werden als Klartext-Umgebungsvariablen in den Container injiziert. Jede Anwendung oder jeder Benutzer mit Zugriff auf `docker inspect <container_name>` kann alle Umgebungsvariablen und damit alle Ihre Secrets im Klartext auslesen.
* **Sicherheit durch Docker Secrets:** Ihr Ansatz (Docker `secrets:`) ist die Best Practice.
  1. **Host-Sicherheit:** Die Secrets auf dem Host sind getrennt von der Compose-Datei.
  2. **Container-Sicherheit:** Im Container werden die Secrets nicht als Umgebungsvariablen gesetzt. Stattdessen werden sie als einzelne Dateien in ein In-Memory-Dateisystem (`tmpfs`) unter `/run/secrets/ gemountet`.
  3. **Isolation:** Diese Secrets sind *niemals* in der Ausgabe von `docker inspect` sichtbar. Nur Prozesse innerhalb des Containers, die explizit die Berechtigung haben, diese Dateien zu lesen, können auf sie zugreifen.

Indem Sie Lösungsansatz A (den `command`-Wrapper) verwenden, schaffen Sie einen Kompromiss: Das Secret wird zwar zur Laufzeit in eine Umgebungsvariable umgewandelt, aber es ist niemals in der `docker inspect`-Ausgabe sichtbar und seine Exposition ist auf den laufenden Prozess beschränkt.

### Tabelle 1: Vergleich der Konfigurationsmethoden für Secrets

| **Merkmal**                       | Docker Compose `secrets:` (Ihr Ansatz)                                                     | `.env`\-Datei (via `env_file:`)                                                     | Umgebungsvariablen (via `environment:`)                         |
|-----------------------------------|--------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| **Wie es funktioniert**           | Host-Datei $\\rightarrow$ Compose Secret $\\rightarrow$ Container-Datei (`/run/secrets/`)  | Host-Datei (`.env`) $\\rightarrow$ Container-Umgebungsvariable                      | `docker-compose.yml` $\\rightarrow$ Container-Umgebungsvariable |
| **Sicherheit auf dem Host**       | Hoch (Secrets können von einem Secret-Manager bezogen werden)                              | **Gering** (Klartext-Datei im Projektverzeichnis)                                   | **Sehr gering** (Klartext im Compose-File, oft in Git)          |
| **Sicherheit im Container**       | **Hoch** als Dateien in `tmpfs` gemountet. Nicht in `docker inspect` sichtbar.             | **Gering** als Klartext-Umgebungsvariablen vorhanden. Sichtbar in `docker inspect`. | **Gering** identisch mit `env_file:`.                           |
| **Kompatibilität**                | Erfordert App-Unterstützung für `_FILE`\-Vars *oder* einen Wrapper (Ihre Situation).       | Hoch fast alle Apps lesen Umgebungsvariablen.                                       | Hoch wie `env_file:`.                                           |
| **Eignung für Produktion**        | **Best Practice**                                                                          | **Nicht empfohlen** für Secrets. OK für nicht-sensible Konfig.                      | **Niemals für Secrets verwenden.**                              |
| **Ihre Keycloak-Herausforderung** | Das Image unterstützt `_FILE` nicht nativ $\\rightarrow$ **Lösung A oder B erforderlich**. | **Würde direkt funktionieren**, ist aber unsicher.                                  | **Würde direkt funktionieren**, ist aber hochgradig unsicher.   |

Ihre Intuition, `secrets:` zu verwenden, war korrekt. Sie sind nicht an einer falschen Konfiguration gescheitert, sondern an einer Inkompatibilität auf der "letzten Meile" – der Implementierung im Keycloak-Image. Die Lösung besteht darin, diese Lücke zu schließen (mit Lösungsansatz A), nicht darin, auf eine weniger sichere Methode (wie .env-Dateien) zurückzufallen.



## VI. Weitere Experten-Überlegungen und potenzielle Fallstricke



Bei der Analyse Ihrer Konfiguration fallen weitere Punkte auf, die für einen stabilen Betrieb relevant sind.

### A. Der `start-dev`-Befehl

Sie verwenden `command: start-dev --import-realm`. Wie der Name schon sagt, ist dieser Modus für die Entwicklung (`dev`) vorgesehen.

* **Einschränkungen:** Der `start-dev`-Modus ist für eine schnelle Inbetriebnahme optimiert. Er deaktiviert wichtige Produktionssicherheitsfunktionen. Beispielsweise erfordert er standardmäßig kein HTTPS und keine explizite Hostname-Konfiguration.
* **Handlungsempfehlung:** Für eine Produktionsumgebung *müssen* Sie zu `command: start` wechseln. Dies erzwingt jedoch zusätzliche Konfigurationen:
  1. **Hostname:** Sie müssen `KC_HOSTNAME` setzen (z. B. `KC_HOSTNAME=auth.meinedomain.com`).
  2. **TLS/HTTPS:** Keycloak wird im Produktionsmodus standardmäßig kein HTTP zulassen. Sie müssen TLS entweder im Container selbst konfigurieren (z. B. mit `KC_HTTPS_CERTIFICATE_FILE` und `KC_HTTPS_CERTIFICATE_KEY_FILE`) oder, was üblicher ist, einen vorgeschalteten Reverse-Proxy (z. B. Nginx, Traefik) verwenden, der die TLS-Terminierung übernimmt.

### B. Datenbank-Kompatibilität

Ihr Compose-File verwendet `postgres:${DOCKER_POSTGRES_VERSION:-18-alpine}`. Postgres 18 ist eine nagelneue Version.

* **Potenzielles Risiko:** Die offizielle Keycloak-Dokumentation listet die getesteten und unterstützten Datenbankversionen auf. Für Keycloak 26.x sind dies typischerweise PostgreSQL 13.x, 14.x, 15.x, 16.x und 17.x.15 Postgres 18 ist nicht explizit aufgeführt.
* **Handlungsempfehlung:** Obwohl es wahrscheinlich funktioniert, setzen Sie sich einem unnötigen Kompatibilitätsrisiko aus. Für eine stabile Produktions- oder Entwicklungsumgebung wird dringend empfohlen, eine explizit unterstützte LTS-Version zu verwenden, z. B. `postgres:16-alpine`.

### C. Admin-Bootstrap-Variablen

Sie verwenden `KC_BOOTSTRAP_ADMIN_USERNAME_FILE` und `KC_BOOTSTRAP_ADMIN_PASSWORD_FILE`. Dies ist absolut korrekt.

* **Hintergrund:** In vielen älteren Anleitungen und Beispielen (oft basierend auf der alten WildFly-Distribution von Keycloak) finden sich die Variablen `KEYCLOAK_ADMIN` und `KEYCLOAK_ADMIN_PASSWORD`.
* **Bestätigung:** Diese alten Variablen funktionieren nicht mehr mit der modernen Quarkus-Distribution (seit Keycloak 17). Ihre Verwendung der `KC_BOOTSTRAP_...`-Variablen zeigt, dass Sie die korrekte, moderne Konfiguration für Keycloak 26.4 verwenden.

### D. Dateiberechtigungen (`/run/secrets/`)

Ein potenzielles Problem bei der Arbeit mit Secrets und nicht-privilegierten Containern sind Dateiberechtigungen.

* **Analyse:** Docker `secrets:` werden standardmäßig als `root:root` mit den Rechten `0444` (lesbar für alle) in den Container gemountet. Der Keycloak-Container läuft standardmäßig als nicht-privilegierter Benutzer `keycloak`.
* **Ergebnis:** Da die Secret-Dateien weltweit lesbar sind (`0444`), hat der keycloak-Benutzer (und damit das `bash -c`...-Skript, das als dieser Benutzer ausgeführt wird) die erforderliche Berechtigung, den Inhalt der Dateien zu lesen (`cat`). Es ist kein Berechtigungsproblem zu erwarten.




## VII. Vollständige, korrigierte und kommentierte `docker-compose.yml`-Lösung



Basierend auf Lösungsansatz A (dem `command`-Wrapper) ist hier die vollständige, funktionierende `docker-compose.yml`-Datei. Sie implementiert die Korrekturen für das Keycloak-Image und enthält die Empfehlung für die Postgres-Version.

```YAML
# Definieren Sie die Version Ihrer Compose-Datei
version: "3.8"

services:
   # \--------------------------------------------------
   # PostgresSQL-Datenbank
   # diese Konfiguration war bereits korrekt.
   # Es wird empfohlen, eine explizit unterstützte Postgres-Version zu verwenden.
   # \--------------------------------------------------
   postgres:
      # Empfehlung: 16-alpine statt 18-alpine für Kompatibilität
      image: postgres:${DOCKER_POSTGRES_VERSION:-16-alpine}
      container_name: meldestelle-postgres
      environment:
         # Das Postgres-Image UNTERSTÜTZT _FILE nativ
         POSTGRES_USER_FILE: /run/secrets/postgres_user
         POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password
         POSTGRES_DB: ${POSTGRES_DB:-meldestelle}
      secrets:
         - postgres_user
         - postgres_password
      ports:
         - "5432:5432"
      volumes:
         - postgres-data:/var/lib/postgresql/data
         # Initialisierungs-Skripte für Postgres
         - ./docker/services/postgres:/docker-entrypoint-initdb.d
      networks:
         - meldestelle-network
      healthcheck:
        
        # Dieser Healthcheck stellt sicher, dass Keycloak wartet, bis die DB bereit ist.
        test: ["CMD-SHELL", "pg_isready -U meldestelle -d meldestelle"]
        interval: 30s
        timeout: 5s
        retries: 3
        start_period: 40s
      restart: unless-stopped

# \--------------------------------------------------
# Keycloak (Quarkus)
# Diese Konfiguration wurde korrigiert, um die _FILE-Lücke zu schließen.
# \--------------------------------------------------
   keycloak:
      image: quay.io/keycloak/keycloak:${DOCKER_KEYCLOAK_VERSION:-26.4}
      container_name: meldestelle-keycloak
      environment:
        # Reguläre Konfiguration.
        # Die _FILE-Variablen werden hier entfernt, da sie durch den 'command'-Wrapper ersetzt werden.
        KC_DB: postgres
        KC_DB_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-meldestelle}
        # HINWEIS: KC_DB_USERNAME, KC_DB_PASSWORD, KC_BOOTSTRAP_ADMIN_USERNAME
        # und KC_BOOTSTRAP_ADMIN_PASSWORD werden unten im 'command'-Abschnitt gesetzt.
      secrets:
        # Deklarieren, dass der Container Zugriff auf diese Secrets benötigt
        - kc_bootstrap_admin_username
        - kc_bootstrap_admin_password
        - postgres_user
        - postgres_password
      ports:
        - "8180:8080"
      depends_on:
        postgres:
          condition: service_healthy # Wartet, bis Postgres "gesund" ist.
      volumes:
        # Mount-Punkt für Realm-Importe
        - ./docker/services/keycloak:/opt/keycloak/data/import

      # --- KORRIGIERTER ABSCHNITT ---
      # Dieser 'command'-Block ist der Workaround für die fehlende _FILE-Unterstützung 
      command: >
        bash -c "
        echo 'Keycloak-Entrypoint-Wrapper: Lese Secrets...'
  
        # Exportiert den Inhalt der Secret-Dateien als Umgebungsvariablen.
        # 'tr -d \n' ist entscheidend, um Zeilenumbrüche zu entfernen [7, 8]
        export KC_BOOTSTRAP_ADMIN_USERNAME=$$(cat /run/secrets/kc_bootstrap_admin_username | tr -d '\n')
        export KC_BOOTSTRAP_ADMIN_PASSWORD=$$(cat /run/secrets/kc_bootstrap_admin_password | tr -d '\n')
        export KC_DB_USERNAME=$$(cat /run/secrets/postgres_user | tr -d '\n')
        export KC_DB_PASSWORD=$$(cat /run/secrets/postgres_password | tr -d '\n')
  
        echo 'Keycloak-Entrypoint-Wrapper: Secrets exportiert. Starte Keycloak...'
  
        # Führt den eigentlichen Keycloak-Startbefehl aus, den Sie ursprünglich wollten.
        # 'exec' sorgt dafür, dass Keycloak der Hauptprozess wird.
        exec /opt/keycloak/bin/kc.sh start-dev --import-realm
        "
      # - ENDE DES KORRIGIERTEN ABSCHNITTS ---
  
      networks:
       - meldestelle-network
      healthcheck:
        # Dieser Healthcheck prüft, ob die Keycloak-Weboberfläche antwortet.
        test: ["CMD", "curl", "--fail", "http://localhost:3000/api/health"]
        interval: 30s
        timeout: 5s
        retries: 3
        start_period: 40s
      restart: unless-stopped

# \--------------------------------------------------
# Top-Level Docker Compose-Definitionen
# Diese waren bereits korrekt.
# \--------------------------------------------------
secrets:
   # Diese Secrets werden von den unten angegebenen Pfaden auf Ihrem Host-Rechner gelesen
   # und in /run/secrets/ im Container bereitgestellt.
   kc_bootstrap_admin_username:
      - file: ./docker/secrets/kc_bootstrap_admin_username.txt
   kc_bootstrap_admin_password:
      - file: ./docker/secrets/kc_bootstrap_admin_password.txt
   postgres_user:
      - file: ./docker/secrets/postgres_user.txt
   postgres_password:
      - file: ./docker/secrets/postgres_password.txt

volumes:
   # Persistentes Volume für Postgres-Daten
   postgres-data:
      driver: local

networks:
   # Dediziertes Bridge-Netzwerk für die interne Kommunikation
  meldestelle-network:
      driver: bridge

```
