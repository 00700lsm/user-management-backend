#!/usr/bin/env bash
# Text-only bootstrap for the official Gradle 9.3.1 Wrapper JAR.
# The downloaded artifact is verified against Gradle's published checksum.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$ROOT/gradle/wrapper/gradle-wrapper.jar"
EXPECTED="b3a875ddc1f044746e1b1a55f645584505f4a10438c1afea9f15e92a7c42ec13"
if [[ ! -f "$JAR" ]]; then
  command -v curl >/dev/null || { echo "curl is required to bootstrap Gradle wrapper" >&2; exit 1; }
  TMP="$JAR.download"
  trap 'rm -f "$TMP"' EXIT
  curl --fail --silent --show-error --location --retry 3 "https://services.gradle.org/distributions/gradle-9.3.1-wrapper.jar" -o "$TMP"
  mv "$TMP" "$JAR"
fi
if command -v sha256sum >/dev/null 2>&1; then
  ACTUAL="$(sha256sum "$JAR" | cut -d' ' -f1)"
elif command -v shasum >/dev/null 2>&1; then
  ACTUAL="$(shasum -a 256 "$JAR" | cut -d' ' -f1)"
else
  echo "sha256sum or shasum required to check wrapper checksum" >&2; exit 1
fi
if [[ "$ACTUAL" != "$EXPECTED" ]]; then
  rm -f "$JAR"
  echo "Wrapper checksum mismatch; jar removed" >&2
  exit 1
fi
exec java -Dorg.gradle.appname=gradlew -cp "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
