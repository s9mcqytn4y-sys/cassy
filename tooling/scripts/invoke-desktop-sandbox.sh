#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
SANDBOX_ROOT="${REPO_ROOT}/.sandbox"
DATA_ROOT="${SANDBOX_ROOT}/desktop-dev"
RESET_DEMO=false
SMOKE_RUN=false
TRUNCATE_DATA=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --reset-demo)
      RESET_DEMO=true
      shift
      ;;
    --smoke-run)
      SMOKE_RUN=true
      shift
      ;;
    --truncate-data)
      TRUNCATE_DATA=true
      shift
      ;;
    --data-root)
      DATA_ROOT="$2"
      shift 2
      ;;
    *)
      echo "Argumen tidak dikenal: $1" >&2
      exit 1
      ;;
  esac
done

mkdir -p "${SANDBOX_ROOT}"
mkdir -p "${DATA_ROOT}"

SANDBOX_ROOT_REAL="$(cd "${SANDBOX_ROOT}" && pwd)"
DATA_ROOT_REAL="$(cd "${DATA_ROOT}" && pwd)"

case "${DATA_ROOT_REAL}" in
  "${SANDBOX_ROOT_REAL}"/*) ;;
  *)
    echo "Data root harus berada di bawah folder sandbox repo: ${SANDBOX_ROOT_REAL}" >&2
    exit 1
    ;;
esac

if [[ "${TRUNCATE_DATA}" == "true" ]]; then
  find "${DATA_ROOT_REAL}" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
fi

export CASSY_DATA_DIR="${DATA_ROOT_REAL}"
if [[ "${RESET_DEMO}" == "true" ]]; then
  export CASSY_DEV_RESET_ENABLED="true"
else
  unset CASSY_DEV_RESET_ENABLED || true
fi

GRADLE_ARGS=(":apps:desktop-pos:run" "--no-configuration-cache" "--console=plain")
if [[ "${RESET_DEMO}" == "true" ]]; then
  GRADLE_ARGS+=("--args=--dev-reset-demo")
elif [[ "${SMOKE_RUN}" == "true" ]]; then
  GRADLE_ARGS+=("--args=--smoke-run")
fi

echo "CASSY_SANDBOX_RUN dataRoot=${DATA_ROOT_REAL} resetDemo=${RESET_DEMO} smokeRun=${SMOKE_RUN} truncateData=${TRUNCATE_DATA}"
cd "${REPO_ROOT}"
./gradlew "${GRADLE_ARGS[@]}"
