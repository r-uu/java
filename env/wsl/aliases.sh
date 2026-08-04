#!/bin/bash
# Project-specific WSL aliases for r-uu-java.
# Loaded by wsl-env bootstrap when ~/.wsl-project points to this repo.
#   ruu-project-set /home/r-uu/develop/github/java

export RUU_JAVA="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
export RUU_LIB="$RUU_JAVA/lib"
export RUU_APP="$RUU_JAVA/app"
export RUU_PRAGMA="$RUU_APP/pragma"
export RUU_PRAGMA_COMPOSE_FILE="${RUU_PRAGMA_COMPOSE_FILE:-$RUU_PRAGMA/docker-compose.yml}"
export RUU_PAPERLESS_CONTAINER="${RUU_PAPERLESS_CONTAINER:-paperless-webserver}"
export RUU_PAPERLESS_COMPOSE_FILE="${RUU_PAPERLESS_COMPOSE_FILE:-$RUU_JAVA/env/wsl/paperless-compose.yml}"
export RUU_PAPERLESS_SERVICE="${RUU_PAPERLESS_SERVICE:-paperless-webserver}"
export RUU_OPENPROJECT_CONTAINER="${RUU_OPENPROJECT_CONTAINER:-openproject}"
export RUU_OPENPROJECT_DB_CONTAINER="${RUU_OPENPROJECT_DB_CONTAINER:-openproject-db}"
export RUU_OPENPROJECT_COMPOSE_FILE="${RUU_OPENPROJECT_COMPOSE_FILE:-$HOME/develop/github/wsl-env/docker/openproject/docker-compose.yml}"
export RUU_OPENPROJECT_SERVICE="${RUU_OPENPROJECT_SERVICE:-openproject}"

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

# Paperless-ngx helpers
# Defaults now point to a local compose stack so ruu-paperless-start/stop work directly.
ruu-paperless-start ()
{
  local container="${RUU_PAPERLESS_CONTAINER:-paperless-webserver}"
  local compose_file="${RUU_PAPERLESS_COMPOSE_FILE:-$RUU_JAVA/env/wsl/paperless-compose.yml}"
  local service="${RUU_PAPERLESS_SERVICE:-paperless-webserver}"

  if ! docker container inspect postgres >/dev/null 2>&1; then
    echo "Shared Postgres container 'postgres' was not found."
    return 1
  fi

  local postgres_running
  postgres_running="$(docker inspect -f '{{.State.Running}}' postgres 2>/dev/null || echo false)"
  if [ "$postgres_running" != "true" ]; then
    docker start postgres >/dev/null 2>&1 || true
  fi

  if [ -n "$compose_file" ] && [ -f "$compose_file" ]; then
    if docker compose -f "$compose_file" config --services 2>/dev/null | grep -qx "$service"; then
      docker compose -f "$compose_file" up -d "$service"
      return $?
    fi
  fi

  if docker container inspect "$container" >/dev/null 2>&1; then
    docker start "$container"
    return $?
  fi

  echo "Container '$container' does not exist and no compose service '$service' was found."
  echo "Set RUU_PAPERLESS_COMPOSE_FILE and RUU_PAPERLESS_SERVICE to the correct compose stack, or create the container first."
  return 1
}

ruu-paperless-stop ()
{
  local container="${RUU_PAPERLESS_CONTAINER:-paperless-webserver}"
  local compose_file="${RUU_PAPERLESS_COMPOSE_FILE:-$RUU_JAVA/env/wsl/paperless-compose.yml}"
  local service="${RUU_PAPERLESS_SERVICE:-paperless-webserver}"

  if [ -n "$compose_file" ] && [ -f "$compose_file" ]; then
    if docker compose -f "$compose_file" config --services 2>/dev/null | grep -qx "$service"; then
      docker compose -f "$compose_file" stop 2>/dev/null || docker compose -f "$compose_file" stop "$service" 2>/dev/null
      return $?
    fi
  fi

  if docker container inspect "$container" >/dev/null 2>&1; then
    docker stop "$container"
    return $?
  fi

  echo "Container '$container' does not exist."
  return 1
}

# JasperReports helpers
ruu-jasper-start ()
{
  local container="jasperreports"

  if docker container inspect "$container" >/dev/null 2>&1; then
    docker start "$container"
    return $?
  fi

  echo "Container '$container' does not exist."
  return 1
}

ruu-jasper-stop ()
{
  local container="jasperreports"

  if docker container inspect "$container" >/dev/null 2>&1; then
    docker stop "$container"
    return $?
  fi

  echo "Container '$container' does not exist."
  return 1
}

ruu-jasper-logs ()
{
  docker logs jasperreports "$@"
}

# OpenProject helpers
ruu-openproject-start ()
{
  local container="${RUU_OPENPROJECT_CONTAINER:-openproject}"
  local compose_file="${RUU_OPENPROJECT_COMPOSE_FILE:-$HOME/develop/github/wsl-env/docker/openproject/docker-compose.yml}"
  local service="${RUU_OPENPROJECT_SERVICE:-openproject}"

  if [ -n "$compose_file" ] && [ -f "$compose_file" ]; then
    if docker compose -f "$compose_file" config --services 2>/dev/null | grep -qx "$service"; then
      docker compose -f "$compose_file" up -d
      return $?
    fi
  fi

  if docker container inspect "$container" >/dev/null 2>&1; then
    docker start "$container"
    return $?
  fi

  echo "Container '$container' does not exist and no compose service '$service' was found."
  echo "Set RUU_OPENPROJECT_COMPOSE_FILE and RUU_OPENPROJECT_SERVICE to the correct compose stack, or create the container first."
  return 1
}

ruu-openproject-stop ()
{
  local container="${RUU_OPENPROJECT_CONTAINER:-openproject}"
  local db_container="${RUU_OPENPROJECT_DB_CONTAINER:-openproject-db}"
  local compose_file="${RUU_OPENPROJECT_COMPOSE_FILE:-$HOME/develop/github/wsl-env/docker/openproject/docker-compose.yml}"
  local service="${RUU_OPENPROJECT_SERVICE:-openproject}"

  if [ -n "$compose_file" ] && [ -f "$compose_file" ]; then
    if docker compose -f "$compose_file" config --services 2>/dev/null | grep -qx "$service"; then
      docker compose -f "$compose_file" stop
      return $?
    fi
  fi

  if docker container inspect "$container" >/dev/null 2>&1; then
    docker stop "$container"
    if docker container inspect "$db_container" >/dev/null 2>&1; then
      docker stop "$db_container" >/dev/null 2>&1 || true
    fi
    return $?
  fi

  echo "Container '$container' does not exist."
  return 1
}

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
echo "  📄 docs:   ruu-paperless-start | ruu-paperless-stop"
echo "  🧾 jasper: ruu-jasper-start | ruu-jasper-stop"
echo "  📋 plan:   ruu-openproject-start | ruu-openproject-stop"
