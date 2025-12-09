# WS 25/26 Programmieren-1 Projekt (STF)

Projekt von [@CyZeTLC](https://github.com/CyZeTLC), [@Phanotmic](https://github.com/Phantomic813), ...

## Überblick

Dieses Projekt wurde im Rahmen des Moduls "Programmieren-1" im Wintersemester 25/26 erstellt. Es handelt sich um ein 2D-Platformer Spiel bei welchem das Ziel ist, alle level zu schaffen und dabei Ordner-Items einzusammeln.

---

## Voraussetzungen

Um das Projekt zu bauen und auszuführen, benötigst du folgende Software:

- **Java Development Kit (JDK):** Version **Amazon Corretto SDK 19.0.2** (oder kompatibles OpenJDK 19+).
- **Apache Maven:** Zum Verwalten der Abhängigkeiten und Bauen des Projekts.
- **MySQL-Datenbank:** Eine laufende Instanz für die Datenhaltung *(In der aktuellen Version noch nicht benötigt)*.

---

## Lokales Setup und Installation

### 1. Repository klonen

Öffne dein Terminal und klone das Repository auf deinen lokalen Rechner:

```bash
git clone https://github.com/CyZeTLC/WS2526-P1.git
cd WS2526-P1
```

### 2. JavaFX Setup

Da das Projekt **JavaFX SDK-17.0.17** nutzt, stelle sicher, dass deine IDE (IntelliJ, Eclipse, VS Code) das SDK korrekt erkennt oder es als Bibliothek eingebunden ist.

---

## Bauen des Projekts

Lege falls nicht vorhanden ein Artifact unter Projekt Struktur->Artifacts für das Modul client an.
Setze ``de.cyzetlc.hsbi.Main`` als Main Klasse.

Danach wähle in der toolbar Build->Build Artifacts.

Navigiere in das Projektverzeichnis und nutze Maven:
Dadurch wird der Code kompiliert und es wird ein ausführbares Artefakt im Ordner `out/artifacts/client_jar` erstellt.

---

## Starten der Anwendung

### Option 1: Über die IDE (Empfohlen)
Importiere das Projekt als **Maven-Projekt**. Erstelle eine "Run Configuration" für die Main-Klasse und füge bei Bedarf die VM-Optionen für JavaFX hinzu:
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

Bei beiden Optionen kann ``/pfad/zu/javafx-sdk-17.0.17/lib`` mit ``PROJECT_DIR/lib/javafx-sdk-17.0.17/lib`` ersetzt werden, wenn man die JavaFX Version mit welcher das Projekt getestet wurde nutzen möchte.

---

## Verwendete Maven Dependencies

| Artefakt | Link | Beschreibung |
| :--- | :--- | :--- |
| **Log4j** | [mvnrepository](https://mvnrepository.com/artifact/log4j/log4j) | Logging-Framework für Fehler- und Statusmeldungen. |
| **Lombok** | [mvnrepository](https://mvnrepository.com/artifact/org.projectlombok/lombok) | Reduziert Boilerplate-Code (Getter, Setter, Konstruktoren) durch Annotationen. |
| **MySQL Connector** | [mvnrepository](https://mvnrepository.com/artifact/mysql/mysql-connector-java) | JDBC-Treiber für die Verbindung zur MySQL-Datenbank. |
| **HikariCP** | [mvnrepository](https://mvnrepository.com/artifact/com.zaxxer/HikariCP) | Performanter JDBC Connection Pool. |
| **Gson** | [mvnrepository](https://mvnrepository.com/artifact/com.google.code.gson/gson) | Bibliothek zur Serialisierung und Deserialisierung von JSON. |
| **Oshi** | [mvnrepository](https://mvnrepository.com/artifact/com.github.oshi/oshi-core) | Native Operating System and Hardware Information (Systeminfos auslesen). |
| **JLine** | [mvnrepository](https://mvnrepository.com/artifact/jline/jline) | Bibliothek für das Handling von Konsoleneingaben. |

---

## Runtime Info

- **Java Version:** Amazon Corretto SDK 19.0.2
- **JavaFX Version:** SDK-17.0.17
