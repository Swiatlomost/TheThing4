#!/usr/bin/env bash
set -euo pipefail

# Helper that CI can invoke to build the Android app with the native PoI ledger.
# Requires:
#   - cargo + cargo-ndk installed and on PATH
#   - rustup available for installing Android targets
#   - ANDROID_SDK_ROOT pointing at an SDK that contains an NDK installation

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ -z "${ANDROID_NDK_HOME:-}" ]]; then
  if [[ -n "${ANDROID_SDK_ROOT:-}" ]] && [[ -d "${ANDROID_SDK_ROOT}/ndk" ]]; then
    latest_ndk="$(
      ls -1 "${ANDROID_SDK_ROOT}/ndk" | sort -Vr | head -n1
    )"
    if [[ -z "$latest_ndk" ]]; then
      echo "No NDK installation found under \$ANDROID_SDK_ROOT/ndk" >&2
      exit 1
    fi
    export ANDROID_NDK_HOME="${ANDROID_SDK_ROOT}/ndk/${latest_ndk}"
  else
    echo "ANDROID_NDK_HOME not set and ANDROID_SDK_ROOT/ndk missing" >&2
    exit 1
  fi
fi
export ANDROID_NDK_ROOT="${ANDROID_NDK_HOME}"

if ! command -v cargo >/dev/null 2>&1; then
  echo "cargo is required for building the native crate" >&2
  exit 1
fi

if ! cargo ndk --version >/dev/null 2>&1; then
  echo "cargo-ndk is required (install via 'cargo install cargo-ndk')" >&2
  exit 1
fi

if command -v rustup >/dev/null 2>&1; then
  rustup target add aarch64-linux-android x86_64-linux-android >/dev/null 2>&1 || true
fi

./gradlew \
  :core:lightledger:cargoBuildLightLedgerArm64 \
  :core:lightledger:cargoBuildLightLedgerX86 \
  :app:assembleDebug \
  -PenableCargoNdk=true
