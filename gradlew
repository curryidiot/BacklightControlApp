#!/usr/bin/env sh

executable="$(dirname "$0")/gradlew.bat"

if [ -x "$executable" ]; then
  "$executable" "$@"
else
  echo "Error: gradlew.bat not found or not executable"
  exit 1
fi