#!/usr/bin/env bash
set -euo pipefail

# Hard prune docs to a strict whitelist. Everything else under docs/ is removed.
# Keep directories required by validator, and the minimal doc set we agreed on.

ROOT_DIR=$(cd "$(dirname "$0")/../.." && pwd)
cd "$ROOT_DIR"

WHITELIST=(
  "docs/index.md"
  "docs/overview/system-overview.md"
  "docs/how-to/start-local.md"
  "docs/how-to/deploy-proxmox-nginx.md"
  "docs/api/README.md"
  "docs/api/members-api.md"        # kept to satisfy link in API README
  "docs/api/generated"             # keep generated OpenAPI specs
  "docs/reference/ports-and-urls.md"
  "docs/now/current.md"
  "docs/now/TEMPLATE.md"
  "docs/now/README.md"
  "docs/proxmox-nginx/meldestelle.conf"
  "docs/architecture/c4"           # keep all C4 diagrams
  "docs/architecture/adr"          # keep all ADRs
  # Note: Do NOT include generic directories like 'docs' or 'docs/api' here,
  # because prefix matching would whitelist everything under them. We'll recreate
  # required empty directories after pruning.
)

is_whitelisted() {
  local path="$1"
  for keep in "${WHITELIST[@]}"; do
    # exact match
    if [[ "$path" == "$keep" ]]; then
      return 0
    fi
    # directory prefix match
    if [[ -d "$keep" && "$path" == $keep/* ]]; then
      return 0
    fi
  done
  return 1
}

# Remove all tracked files under docs/ that are not whitelisted
mapfile -t FILES < <(git ls-files docs)

REMOVED=0
for f in "${FILES[@]}"; do
  if ! is_whitelisted "$f"; then
    git rm -r -q "$f"
    ((REMOVED++)) || true
  fi
done

echo "Removed $REMOVED files not in whitelist."

# Ensure required directories exist (even if empty) for validator
mkdir -p docs docs/api docs/architecture docs/development docs/how-to docs/overview docs/now docs/proxmox-nginx docs/reference

# Optionally remove empty directories that are not needed anymore (except required ones)
# Find empty directories under docs and remove them, but skip the required ones above
find docs -type d -empty \
  ! -path "docs" \
  ! -path "docs/api" \
  ! -path "docs/architecture" \
  ! -path "docs/development" \
  ! -path "docs/how-to" \
  ! -path "docs/overview" \
  ! -path "docs/now" \
  ! -path "docs/proxmox-nginx" \
  ! -path "docs/reference" \
  -print -delete || true

# Stage only docs directory updates created by find
git add -A docs

echo "Docs prune completed. Review changes and commit."
