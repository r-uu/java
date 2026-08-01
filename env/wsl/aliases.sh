#!/bin/bash
# Project-specific WSL aliases for r-uu-java.
# Loaded by wsl-env bootstrap when ~/.wsl-project points to this repo.
#   ruu-project-set /home/r-uu/develop/github/java

export RUU_JAVA="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
export RUU_LIB="$RUU_JAVA/lib"
export RUU_APP="$RUU_JAVA/app"
export RUU_PRAGMA="$RUU_APP/pragma"
export RUU_PRAGMA_COMPOSE_FILE="${RUU_PRAGMA_COMPOSE_FILE:-$RUU_PRAGMA/docker-compose.yml}"

ruu-autostart-infra() {
  [ "${RUU_WSL_AUTOSTART_INFRA:-1}" = "1" ] || return 0
  [ -n "${RUU_AUTOSTART_INFRA_DONE:-}" ] && return 0
  export RUU_AUTOSTART_INFRA_DONE=1

  command -v docker >/dev/null 2>&1 || return 0
  docker info >/dev/null 2>&1 || return 0

  local compose_file="$RUU_PRAGMA_COMPOSE_FILE"
  [ -f "$compose_file" ] || return 0

  docker compose -f "$compose_file" config --services 2>/dev/null | grep -qx 'keycloak' || return 0

  local keycloak_cid
  local keycloak_state
  keycloak_cid="$(docker compose -f "$compose_file" ps -q keycloak 2>/dev/null | head -n1)"
  if [ -n "$keycloak_cid" ]; then
    keycloak_state="$(docker inspect -f '{{.State.Running}}' "$keycloak_cid" 2>/dev/null || echo false)"
    [ "$keycloak_state" = "true" ] && return 0
  fi

  local start_output
  if ! start_output="$(
    docker compose -f "$compose_file" up -d keycloak 2>&1
  )"; then
    echo "⚠️  Could not auto-start pragma postgres/keycloak (compose file: $compose_file)"
    echo "$start_output"
  fi
}

ruu-autostart-infra

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

# ═══════════════════════════════════════════════════════════════════
# Pragma — Dev/Test Login-Shortcuts (Keycloak)
# ═══════════════════════════════════════════════════════════════════
ruu-pragma-fx-as() {
  local user="${1:-r-uu}"
  local pass="${2:-$user}"
  shift 2 || true
  cd "$RUU_PRAGMA" || return 1
  mvn -pl frontend/fx -am exec:java@pragma \
    -Dpragma.keycloak.username="$user" \
    -Dpragma.keycloak.password="$pass" \
    "$@"
}
alias ruu-pragma-fx='ruu-pragma-fx-as r-uu r-uu'
alias ruu-pragma-fx-admin='ruu-pragma-fx-as admin admin'

echo "✓  r-uu-java aliases loaded"
echo "  📚 help:   ruu-help | ruu-groups"
echo "  🔨 build:  ruu-mvn-install-fast"
