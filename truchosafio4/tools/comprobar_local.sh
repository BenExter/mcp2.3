#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source tools/entorno.sh
python3 VALIDAR_RECURSOS.py
