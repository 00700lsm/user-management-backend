#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SHA="$(tr -d '[:space:]' < "$ROOT/contract.sha")"
[[ "$SHA" =~ ^[a-f0-9]{40}$ ]] || { echo "Invalid contract.sha"; exit 1; }
DEST="$ROOT/.contract-cache"
mkdir -p "$DEST"
curl --fail --location --retry 3 "https://raw.githubusercontent.com/00700lsm/user-management-contract/$SHA/openapi.yaml" -o "$DEST/openapi.yaml"
echo "Contract pinned to $SHA ($DEST/openapi.yaml)"
