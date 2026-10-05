#!/usr/bin/env bash
set -euo pipefail
if ! command -v java >/dev/null; then echo "Java 17+ is required"; exit 1; fi
if [ ! -d "$ANDROID_HOME" ] && [ ! -d "$ANDROID_SDK_ROOT" ]; then echo "ANDROID_HOME/ANDROID_SDK_ROOT is required to build the APK"; exit 1; fi
./gradlew :app:assembleDebug
