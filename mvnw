#!/bin/sh
if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven is not installed. Install Maven 3.9+ or run the project from IntelliJ IDEA." >&2
  exit 1
fi
exec mvn "$@"
