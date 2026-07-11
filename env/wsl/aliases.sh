#!/bin/bash
# Project-specific WSL aliases for r-uu-java.
# Loaded by wsl-env bootstrap when ~/.wsl-project points to this repo.
#   ruu-project-set /home/r-uu/develop/github/r-uu-java

export RUU_JAVA="/home/r-uu/develop/github/r-uu-java"
export RUU_LIB="$RUU_JAVA/lib"
export RUU_APP="$RUU_JAVA/app"
export RUU_PRAGMA="$RUU_APP/pragma"

# ═══════════════════════════════════════════════════════════════════
# Navigation
# ═══════════════════════════════════════════════════════════════════
alias ruu-cd-root='cd $RUU_JAVA'
alias ruu-cd-lib='cd $RUU_LIB'
alias ruu-cd-app='cd $RUU_APP'
alias ruu-cd-pragma='cd $RUU_PRAGMA'

# ═══════════════════════════════════════════════════════════════════
# Maven — Build
# ═══════════════════════════════════════════════════════════════════
alias ruu-mvn-install='cd $RUU_JAVA && mvn clean install'
alias ruu-mvn-install-fast='cd $RUU_JAVA && mvn clean install -DskipTests'
alias ruu-mvn-install-lib='cd $RUU_LIB && mvn clean install'
alias ruu-mvn-install-lib-fast='cd $RUU_LIB && mvn clean install -DskipTests'
alias ruu-mvn-install-pragma='cd $RUU_PRAGMA && mvn clean install'
alias ruu-mvn-install-pragma-fast='cd $RUU_PRAGMA && mvn clean install -DskipTests'
alias ruu-mvn-test='cd $RUU_JAVA && mvn test'
alias ruu-mvn-clean='cd $RUU_JAVA && mvn clean'

# ═══════════════════════════════════════════════════════════════════
# Git
# ═══════════════════════════════════════════════════════════════════
alias ruu-git-status='cd $RUU_JAVA && git status'
alias ruu-git-pull='cd $RUU_JAVA && git pull'
alias ruu-git-push='cd $RUU_JAVA && git push'
alias ruu-git-log='cd $RUU_JAVA && git log --oneline --graph --all -20'

# ═══════════════════════════════════════════════════════════════════
# Pragma — Windows Executable
# ═══════════════════════════════════════════════════════════════════
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

echo "✓  r-uu-java aliases loaded"
echo "  📚 help:   ruu-help | ruu-groups"
echo "  🔨 build:  ruu-mvn-install-fast"
