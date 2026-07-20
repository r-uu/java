#!/bin/bash
# Project-specific WSL aliases for app-pragma-java.
# Loaded by wsl-env bootstrap when ~/.wsl-project points to this repo.
#   ruu-project-set /home/r-uu/develop/github/java/app/pragma

export RUU_PRAGMA="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# ═══════════════════════════════════════════════════════════════════
# Pragma
# ═══════════════════════════════════════════════════════════════════
alias ruu-pragma-cd='cd $RUU_PRAGMA'

# Baut ein selbstständiges Windows-App-Image unter C:\Users\r-uu\develop\win-exe\pragma\:
#   pragma.exe  – nativer Launcher (kein installiertes JDK nötig)
#   runtime\    – zugeschnittenes JRE (jlink, nur benötigte JDK-Module)
#   app\        – pragma.jar + lib\ (JavaFX-Win-JARs, per --module-path geladen)
ruu-pragma-win-exe() {
  cd "$RUU_PRAGMA" || return 1

  mvn package -pl frontend/fx -am -P win-exe || return 1

  local win_dest='C:\Users\r-uu\develop\win-exe'
  local win_input
  win_input="$(wslpath -w "$RUU_PRAGMA/frontend/fx/target/win-exe")"

  cmd.exe /c "if exist ${win_dest}\pragma rmdir /s /q ${win_dest}\pragma"

  /mnt/c/software/develop/jdk/bin/jpackage.exe \
    --type app-image \
    --name pragma \
    --app-version 0.0.1 \
    --win-console \
    --input "${win_input}" \
    --main-jar pragma.jar \
    --java-options '--module-path $APPDIR\lib' \
    --java-options '--add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics' \
    --java-options '--enable-native-access=javafx.graphics' \
    --add-modules java.base,java.desktop,java.logging,java.naming,java.management,java.net.http,java.rmi,java.scripting,java.sql,java.transaction.xa,java.xml,java.prefs,java.security.sasl,jdk.unsupported,jdk.zipfs \
    --dest "${win_dest}"
}
alias ruu-pragma-exe='ruu-pragma-win-exe'

echo "✓  pragma aliases loaded"
