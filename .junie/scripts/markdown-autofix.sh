#!/usr/bin/env bash
set -euo pipefail

# Markdown auto-fix helper
# - Runs markdownlint in --fix mode over all Markdown files
# - Normalizes final newline at EOF (MD047)
# - Respects repo config (.markdownlint.yaml, .markdownlintignore)

ROOT_DIR=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd "$ROOT_DIR"

# Ensure dependencies are available (prefer locally installed first)
if ! command -v markdownlint >/dev/null 2>&1; then
  if command -v npm >/dev/null 2>&1; then
    echo "[INFO] Installing markdownlint-cli globally (requires npm) ..."
    npm i -g markdownlint-cli >/dev/null 2>&1 || true
  fi
fi

# As fallback, use npx if markdownlint is still not found
RUN_MDLINT="markdownlint"
if ! command -v markdownlint >/dev/null 2>&1; then
  if command -v npx >/dev/null 2>&1; then
    RUN_MDLINT="npx -y markdownlint-cli"
  else
    echo "[ERROR] markdownlint-cli not found and npm/npx unavailable. Please install Node.js (>= 18) first." >&2
    exit 1
  fi
fi

echo "[INFO] Running markdownlint --fix over all Markdown files ..."
# shellcheck disable=SC2086
$RUN_MDLINT \
  --fix \
  --config .markdownlint.yaml \
  --ignore-path .markdownlintignore \
  "**/*.md" || true

# Normalize EOF: ensure exactly one trailing newline (MD047) for all tracked Markdown files
# Uses Perl to replace any trailing whitespace/newlines with a single newline.
if command -v perl >/dev/null 2>&1; then
  echo "[INFO] Normalizing end-of-file newlines (MD047) ..."
  git ls-files "*.md" | while read -r f; do
    perl -0777 -pe 's/\s*\z/\n/' "$f" > "$f.tmp.$RANDOM" && mv "$f.tmp."* "$f"
  done
else
  echo "[WARN] Perl not found; skipping explicit MD047 normalization. EditorConfig may cover this on save."
fi

# Show summary of changes
if ! git diff --quiet; then
  echo "[INFO] Changes made by auto-fix:";
  git --no-pager diff --stat
  echo "[HINT] Review and commit changes:";
  echo "       git add -A && git commit -m \"chore(docs): markdown auto-fix\""
else
  echo "[INFO] No changes required. Markdown files already conform to rules."
fi
