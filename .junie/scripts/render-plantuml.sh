#!/usr/bin/env bash
set -euo pipefail

mkdir -p build/diagrams
shopt -s nullglob
for f in docs/architecture/c4/*.puml; do
  docker run --rm -v "$PWD":/data plantuml/plantuml -tsvg "/data/$f" -o "/data/build/diagrams"
  echo "Rendered build/diagrams/$(basename "${f%.puml}").svg"
done
