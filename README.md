<div align="center">

# WS 25/26 Programmieren-1 Projekt (STF)

Projekt von [@CyZeTLC](https://github.com/CyZeTLC), [@Phanotmic](https://github.com/Phantomic813)

<a href="https://github.com/CyZeTLC/WS2526-P1/graphs/contributors">
  <img src="https://img.shields.io/github/contributors/CyZeTLC/WS2526-P1" alt="GitHub contributors">
</a>
<a href="https://opensource.org/licenses/Apache-2.0">
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License">
</a>
<a href="https://github.com/CyZeTLC/WS2526-P1/actions">
  <img src="https://img.shields.io/github/actions/workflow/status/CyZeTLC/WS2526-P1/main.yml" alt="GitHub Workflow Status">
</a>

</div>

## Überblick

Dieses Projekt wurde im Rahmen des Moduls "Programmieren-1" im Wintersemester 25/26 erstellt. Es handelt sich um ein 2D-Platformer Spiel, bei welchem das Ziel ist, alle level zu schaffen und dabei Ordner-Items einzusammeln.

---

## Voraussetzungen

Um das Projekt zu bauen und auszuführen, benötigst du folgende Software:

- **Java Development Kit (JDK):** Version **Amazon Corretto SDK 19.0.2** (oder kompatibles OpenJDK 19+).
- **Apache Maven:** Zum Verwalten der Abhängigkeiten und Bauen des Projekts.
- **IntelIJ IDEA**: Zum Starten/Bauen des Projektes (IDEs wie Eclipse könnten auch funktionieren)

---

## Lokales Setup und Installation

### 1. Repository klonen

Öffne dein Terminal und klone das Repository auf deinen lokalen Rechner:

```bash
git clone https://github.com/CyZeTLC/WS2526-P1.git
cd WS2526-P1
```

---

## Starten der Anwendung

Die wichtigste Anforderung ist, dass die config.json Datei existiert. Diese muss im Ordner der Jar oder im Baseordner des Projekts liegen. Sollte das Projekt aus der Repo geladen worden, so ist diese Datei bereits vorhanden.

### Option 1: Über die IDE (Empfohlen)
Importiere das Projekt als **Maven-Projekt**. Erstelle eine "Run Configuration" für die Main-Klasse und füge bei Bedarf die VM-Optionen für JavaFX hinzu (im Normalfall nicht benötigt):
```text
--module-path /pfad/zu/javafx-sdk-17.0.17/lib --add-modules javafx.controls,javafx.fxml
```

### Option 2: Über das Terminal (JAR)
Nach dem Build kann die JAR-Datei gestartet werden (Pfade anpassen):

```bash
java --module-path /pfad/zu/javafx-sdk-17.0.17/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/client.jar
```

Bei beiden Optionen können die Startargumente im Normalfall ohne Probleme weggelassen werden.

---

## Bauen des Projekts

Lege falls nicht vorhanden ein Artifact unter Projekt Struktur->Artifacts für das Modul client an.
Setze ``de.cyzetlc.hsbi.Main`` als Main Klasse.

Danach wähle in der toolbar Build->Build Artifacts.

Navigiere in das Projektverzeichnis und nutze Maven:
Dadurch wird der Code kompiliert und es wird ein ausführbares Artefakt im Ordner `out/artifacts/client_jar` erstellt.

---

## Verwendete Maven Dependencies

| Artefakt | Link | Beschreibung |
| :--- | :--- | :--- |
| **Log4j** | [mvnrepository](https://mvnrepository.com/artifact/log4j/log4j) | Logging-Framework für Fehler- und Statusmeldungen. |
| **Lombok** | [mvnrepository](https://mvnrepository.com/artifact/org.projectlombok/lombok) | Reduziert Boilerplate-Code (Getter, Setter, Konstruktoren) durch Annotationen. |
| **MySQL Connector** | [mvnrepository](https://mvnrepository.com/artifact/mysql/mysql-connector-java) | JDBC-Treiber für die Verbindung zur MySQL-Datenbank. |
| **HikariCP** | [mvnrepository](https://mvnrepository.com/artifact/com.zaxxer/HikariCP) | Performanter JDBC Connection Pool für effiziente Datenbankverbindungen. |
| **Gson** | [mvnrepository](https://mvnrepository.com/artifact/com.google.code.gson/gson) | Bibliothek zur Serialisierung und Deserialisierung von JSON-Daten. |
| **JavaFX Controls** | [mvnrepository](https://mvnrepository.com/artifact/org.openjfx/javafx-controls) | **Das Hauptmodul für die GUI-Elemente** (Buttons, Labels, Checkboxen, etc.). |
| **JavaFX FXML** | [mvnrepository](https://mvnrepository.com/artifact/org.openjfx/javafx-fxml) | Ermöglicht das **Deklarieren der Benutzeroberfläche** in XML-Dateien, getrennt von der Java-Logik. |
| **JavaFX Graphics** | [mvnrepository](https://mvnrepository.com/artifact/org.openjfx/javafx-graphics) | Stellt die Kern-API für das Rendern von Grafiken bereit (wird von Controls/FXML benötigt). |
| **JavaFX Media** | [mvnrepository](https://mvnrepository.com/artifact/org.openjfx/javafx-media) | Modul zur Integration von Audio und Video (z.B. Player-Funktionalität). |

---

## Runtime Info

- **Java Version:** Amazon Corretto SDK 19.0.2
- **JavaFX Version:** SDK-17.0.6
