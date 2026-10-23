#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source tools/entorno.sh
python3 VALIDAR_RECURSOS.py
truchosafio_test_dir=$(mktemp -d /tmp/truchosafio-tests.XXXXXX)
trap 'rm -rf "$truchosafio_test_dir"' EXIT
javac -d "$truchosafio_test_dir" src/main/java/dev/truchosafio4/logic/MechanismShield.java src/test/java/dev/truchosafio4/logic/MechanismShieldTest.java
java -cp "$truchosafio_test_dir" dev.truchosafio4.logic.MechanismShieldTest
