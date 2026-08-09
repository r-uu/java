#!/bin/bash
# Project-specific WSL aliases for app-pragma-java.
# Loaded by wsl-env bootstrap when ~/.wsl-project points to this repo.
#   ruu-project-set /home/r-uu/develop/github/java/app/pragma

export RUU_PRAGMA="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
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
# Pragma
# ═══════════════════════════════════════════════════════════════════
alias ruu-pragma-cd='cd $RUU_PRAGMA'

ruu-docker-up() {
  docker compose -f "$RUU_PRAGMA_COMPOSE_FILE" up -d "$@"
}

ruu-docker-restart-postgres() {
  docker compose -f "$RUU_PRAGMA_COMPOSE_FILE" restart postgres
}

ruu-keycloak-start() {
  docker compose -f "$RUU_PRAGMA_COMPOSE_FILE" up -d keycloak
}

ruu-keycloak-logs() {
  docker logs pragma-keycloak "$@"
}

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

# Dev/Test shortcut: JavaFX app with preset Keycloak user login.
ruu-keycloak-provision() {
  local user="${1:-r-uu}"
  local pass="${2:-$user}"
  local admin_user="${3:-admin-keycloak}"
  local admin_pass="${4:-r-uu}"
  if [ "$#" -ge 4 ]; then
    shift 4 || true
  else
    shift 2 || true
  fi
  cd "$RUU_PRAGMA" || return 1
  mvn -f ../../lib/keycloak/admin/pom.xml -am exec:java \
    -Dexec.mainClass=de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup \
    -Dkeycloak.admin.user=admin \
    -Dkeycloak.admin.password=admin \
    -Dkeycloak.realm=pragma-realm \
    -Dkeycloak.client.id=pragma-frontend \
    -Dapp.test.user="$user" \
    -Dapp.test.password="$pass" \
    "$@" || return 1
  mvn -f ../../lib/keycloak/admin/pom.xml -am exec:java \
    -Dexec.mainClass=de.ruu.lib.keycloak.admin.setup.KeycloakRealmSetup \
    -Dkeycloak.admin.user=admin \
    -Dkeycloak.admin.password=admin \
    -Dkeycloak.realm=pragma-realm \
    -Dkeycloak.client.id=pragma-frontend \
    -Dapp.test.user="$admin_user" \
    -Dapp.test.password="$admin_pass" \
    "$@"
}

ruu-keycloak-setup() {
  ruu-keycloak-provision "$@"
}

ruu-pragma-fx-as() {
  local user="${1:-r-uu}"
  local pass="${2:-$user}"
  shift 2 || true
  ruu-keycloak-provision "$user" "$pass" "$@" && \
  cd "$RUU_PRAGMA" && \
  mvn -pl frontend/fx -am exec:java@pragma \
    -Dpragma.keycloak.username="$user" \
    -Dpragma.keycloak.password="$pass" \
    "$@"
}
alias ruu-pragma-fx='ruu-pragma-fx-as r-uu r-uu'
alias ruu-pragma-fx-admin='ruu-pragma-fx-as admin-keycloak r-uu'

echo "✓  pragma aliases loaded"
