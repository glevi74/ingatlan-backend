#!/usr/bin/env bash
# RSA kulcspár generálása JWT aláíráshoz
# Futtatás: bash generate-keys.sh
set -e

OUT="src/main/resources"

echo "RSA kulcspár generálása..."
openssl genrsa -out "$OUT/privateKey.pem" 2048
openssl rsa -pubout -in "$OUT/privateKey.pem" -out "$OUT/publicKey.pem"

echo ""
echo "Kész! Fájlok helye:"
echo "  $OUT/privateKey.pem  (SOHA ne commitold!)"
echo "  $OUT/publicKey.pem"
echo ""
echo "Ellenőrizd, hogy a .gitignore tartalmazza:"
echo "  src/main/resources/privateKey.pem"
