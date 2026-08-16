#!/bin/bash
# Hetzner server aliases for the hosted r-uu setup.

export RUU_STACKS="${RUU_STACKS:-/opt/stacks}"
export RUU_CADDY_STACK="${RUU_CADDY_STACK:-$RUU_STACKS/caddy}"
export RUU_DMS_STACK="${RUU_DMS_STACK:-$RUU_STACKS/dms}"
export RUU_PRAGMA_STACK="${RUU_PRAGMA_STACK:-$RUU_STACKS/pragma}"
export RUU_CADDY_COMPOSE="${RUU_CADDY_COMPOSE:-$RUU_CADDY_STACK/docker-compose.yml}"
export RUU_DMS_COMPOSE="${RUU_DMS_COMPOSE:-$RUU_DMS_STACK/docker-compose.yml}"
export RUU_PRAGMA_COMPOSE="${RUU_PRAGMA_COMPOSE:-$RUU_PRAGMA_STACK/docker-compose.yml}"
export RUU_DMS_CONTAINER="${RUU_DMS_CONTAINER:-dms}"
export RUU_PRAGMA_CONTAINER="${RUU_PRAGMA_CONTAINER:-pragma}"

alias ruu-cd-stacks='cd $RUU_STACKS'
alias ruu-cd-caddy='cd $RUU_CADDY_STACK'
alias ruu-cd-dms='cd $RUU_DMS_STACK'
alias ruu-cd-pragma='cd $RUU_PRAGMA_STACK'

ruu-caddy-up() {
  docker compose -f "$RUU_CADDY_COMPOSE" up -d "$@"
}

ruu-caddy-logs() {
  docker compose -f "$RUU_CADDY_COMPOSE" logs "$@"
}

ruu-dms-up() {
  docker compose -f "$RUU_DMS_COMPOSE" up -d "$@"
}

ruu-dms-logs() {
  docker compose -f "$RUU_DMS_COMPOSE" logs "$@"
}

ruu-paperless-create-superuser() {
  local container="${1:-$RUU_DMS_CONTAINER}"
  docker exec -it "$container" /bin/sh -lc '
    if command -v paperless-manage >/dev/null 2>&1; then
      exec paperless-manage createsuperuser
    fi
    if command -v python3 >/dev/null 2>&1; then
      exec python3 /app/paperless/src/manage.py createsuperuser
    fi
    echo "No suitable paperless user-management command found in container." >&2
    exit 1
  '
}

ruu-paperless-changepassword() {
  local user="${1:?usage: ruu-paperless-changepassword <username> [container]}"
  local container="${2:-$RUU_DMS_CONTAINER}"
  docker exec -it "$container" /bin/sh -lc "
    if command -v paperless-manage >/dev/null 2>&1; then
      exec paperless-manage changepassword '$user'
    fi
    if command -v python3 >/dev/null 2>&1; then
      exec python3 /app/paperless/src/manage.py changepassword '$user'
    fi
    echo 'No suitable paperless user-management command found in container.' >&2
    exit 1
  "
}

echo "✓  hetzner aliases loaded"
echo "  ☁️  caddy:     ruu-cd-caddy | ruu-caddy-up | ruu-caddy-logs"
echo "  📄 dms:       ruu-cd-dms | ruu-dms-up | ruu-dms-logs"
echo "  👤 paperless: ruu-paperless-create-superuser | ruu-paperless-changepassword"
