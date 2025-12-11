# StealTheFiles - Dokumentation

Wintersemester 2025/26 – HSBI Campus Minden Programmieren 1 - Testat – Autoren: Tom Coombs, Leonardo Rosario Parrino 

---
## Inhaltsverzeichnis

- [1. Konzept & Projektidee](#1-konzept--projekteidee)
- [2. Entwicklungsumgebung & genutzte Dependencies](#2-entwicklungsumgebung--genutzte-dependencies)
  - [2.1 Entwicklungsumgebung & Java Versionen](#21-entwicklungsumgebung--java-versionen)
  - [2.2 Maven Dependencies](#22-maven-dependencies)
  - [Erklärung zur Wahl der Dependencies](#22-maven-dependencies)
- [3. LLMs](#3-llms)
  - [Verwendetes Modell & Prompts](#3-llms)
---

## 1. Konzept & Projekteidee

- Allgemein soll es einen einfachen 2D-Platformer abbilden.
- Es gibt einen Spieler, welcher versucht bis zum Level-Ziel zu kommen und dabei so viele Ordner (mit Klausuren) wie möglich einsammelt.
  - Dieser Ordner befinden sich über das ganze Level verteilt.
- Die Steuerung soll sich mit `A`, `D` und `SPACE` einfach halten und ein intuitives Spielerlebnis bieten.
- Geplant war auch die Umsetzung eines Mehrspielermodus, welcher ermöglichen soll, in einer großen gemeinsamen Map zu spielen.

## 2. Entwicklungsumgebung & genutzte Dependencies 

### 2.1 Entwicklungsumgebung & Java Versionen
- **IDE:** IntelliJ IDEA (Community Edition)
- **Java Version:** Amazon Corretto SDK 19.0.2
- **JavaFX Version:** SDK-17.0.6

### 2.2 Maven Dependencies

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

**Erklärung warum sich für bestimmte Dependencies entschieden wurde:**

- Lombok: Dieses Tool nutze ich bereits seit vielen Jahren und dadurch weiß ich gut, wie hilfreich es sein kann, um einiges ein Code zu sparen. Vor allem in Klassen mit vielen Attributen ist Lombok eines der besten Tools um eine gute und organisierte Codestruktur zu behalten.
- Log4j: Das Tool ist nur für das Loggen vorhanden. Auch wenn unser Projekt nicht allzu viele Stellen hat, wo etwas geloggt wird, ist es trotzdem hilfreich, um in späteren Szenarien bereits eine gute Codestruktur zu haben, bei welcher die Implementation eines Loggingsystem super einfach ist.
- HikariCP & MySQL: Dieses beiden Dependencies sind im Server-Modul vorhanden und dienen schlicht dazu eine Datenbankverbindung herzustellen. HikariCP ist dazu der aus meiner Erfahrung mit Abstand performanteste JDBC-Verbindungspool und ermöglicht eine stabile Datenbankverbindung mit sehr geringer Latenz.
- Gson: Gson wird einfach nur genutzt um die JsonConfig einfach zu Verwalten und um aus einer JSON-Datei eine Javaklasse zu generieren.

## 3. LLMs

Allgemein wurden in diesem Projekt so gut wie keine LLMs zur Generierung des Quelltextes verwendet. Der Hintergrund ist vor allem, dass ich (Tom) bereits fast eine Dekade an Programmiererfahrung und viele Jahre in IT-Unternehmen als Fullstack-Developer habe und somit bereits einiges an Kennwissen besitze. 
<br>
Trotzdem wurden für Teillösungen LLMs genutzt, da es zum einen Aufgabe war und zum anderen LLMs wie Copilot, Tabnine oder Codex genutzt.
<br>
Dabei konnte auch festgestellt werden, dass LLMs für einige Probleme sehr hilfreich sind. Vor allem bei Probleme welche normaler weiße etwas komplexer sind, wie z.B. die Implementierung der Kamera, konnten die LLMs Teile unserer Probleme bei der Implementation einer solchen beheben. 

**Verwendetes Modell: Gemini 2.0 Flash Thinking**

<details>
<summary><strong>Hier klicken, um die verwendeten Prompts anzuzeigen</strong></summary>

<br>

**Prompt 1: Der Überblick (Design-Fokus)**
> "Ich entwickle einen 2D-Platformer in JavaFX und suche nach einer besseren Kamera-Lösung als der starren Spieler-Fixierung. Kannst du mir eine Übersicht der gängigen Systeme (z.B. Deadzones, Smoothing, Look-Ahead) geben? Mich interessieren besonders die Auswirkungen auf das 'Game Feel' und welches Genre typischerweise welche Methode nutzt, damit ich das richtige Konzept für mein Spiel wählen kann."

**Prompt 2: Die Technik (Mathematik & Code-Logik)**
> "Mein JavaFX-Kameracode (hartes setTranslateX) wirkt steif und ruckelig. Ich möchte das flüssige Verhalten moderner Platformer (Hollow Knight) erreichen. Kannst du mir die Mathematik hinter 'Position Smoothing' (Lerp) erklären und wie ich das sauber in einen AnimationTimer einbaue? Wichtig ist mir auch das 'Clamping', damit die Kamera nicht über den Levelrand hinausfährt. Ich möchte die Logik verstehen, um sie selbst zu implementieren."

**Prompt 3: Die Entscheidung (Pro/Contra Analyse)**
> "Ich muss eine fundierte Design-Entscheidung für meine Kamera treffen. Bitte erstelle mir eine Vergleichsanalyse (Vor-/Nachteile) für: Locked, Deadzone, Smoothing und Look-Ahead. Bewerte sie bitte nach Implementierungsaufwand, Game Feel/UX und Genre-Eignung. Ich brauche diese Argumente, um meine Wahl im Projekt technisch begründen zu können."

</details>
