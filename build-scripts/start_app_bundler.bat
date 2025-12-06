@echo off
REM Setze Umgebungsvariable LOCAL_JAVAFX_SDK_LIB auf den Pfad zum JavaFX-SDK/lib-Ordner
set LOCAL_JAVAFX_SDK_LIB="C:\Users\Tom\Documents\javafx-sdk-17.0.17\lib"

REM Pfad zur jpackage.exe
set JPACKAGE_EXE="C:\Program Files\Java\jdk-17.0.1\bin\jpackage.exe"

REM Zuerst die Ausgabe und temporäre Ordner löschen
if exist installers rmdir /s /q installers
if exist temp_build rmdir /s /q temp_build
mkdir installers

REM OHNE --type und mit --temp, um das Anwendungs-Image zu erzwingen.
%JPACKAGE_EXE% --name "StealTheFiles" --input app-content --dest ./installers --main-class de.cyzetlc.hsbi.Main --main-jar client_obfuscated.jar --module-path %LOCAL_JAVAFX_SDK_LIB% --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base --app-version "1.0" --vendor "Tom C." --temp temp_build

echo.
echo JPackage-Vorgang abgeschlossen. Das fertige Image ist nun im Ordner .\installers\StealTheFiles.
pause
