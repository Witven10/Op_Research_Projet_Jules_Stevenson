#!/bin/bash
# Build et lance le projet.
# Usage :
#   ./build.sh            # compile et lance tous les tests
#   ./build.sh --file F   # compile et lance sur un fichier graphe au format prof
set -e
cd "$(dirname "$0")"
mkdir -p build
javac -d build src/opresearch/*.java
echo "[build] OK"
java -Dfile.encoding=UTF-8 -cp build opresearch.Main "$@"
