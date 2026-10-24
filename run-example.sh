#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_PATH="$SCRIPT_DIR/example.jar"

if [[ ! -f "$JAR_PATH" ]]; then
    printf 'Error: %s was not found.\n' "$JAR_PATH" >&2
    exit 1
fi

case "$(uname -s)" in
    Linux) OS="linux" ;;
    Darwin) OS="mac" ;;
    *)
        printf 'Error: this script supports Linux and macOS only.\n' >&2
        exit 1
        ;;
esac

case "$(uname -m)" in
    x86_64|amd64) ARCH="x64" ;;
    arm64|aarch64) ARCH="aarch64" ;;
    *)
        printf 'Error: unsupported machine architecture: %s\n' "$(uname -m)" >&2
        exit 1
        ;;
esac

RUNTIME_ROOT="$SCRIPT_DIR/.java-runtime"
RUNTIME_DIR="$RUNTIME_ROOT/temurin-24-$OS-$ARCH"

JAVA_BIN=""
SYSTEM_JAVA="$(command -v java 2>/dev/null || true)"
if [[ -n "$SYSTEM_JAVA" ]]; then
    SYSTEM_JAVA_VERSION="$("$SYSTEM_JAVA" -version 2>&1 | sed -nE '1{s/.*version "([0-9]+)(\.[0-9]+)*".*/\1/p;}' || true)"
    if [[ "$SYSTEM_JAVA_VERSION" == "24" ]]; then
        JAVA_BIN="$SYSTEM_JAVA"
        printf 'Using the system Java 24 installation.\n'
    fi
fi

find_java() {
    if [[ -d "$1" ]]; then
        find "$1" -path '*/bin/java' -print -quit 2>/dev/null
    fi
}

if [[ -z "$JAVA_BIN" ]]; then
    JAVA_BIN="$(find_java "$RUNTIME_DIR")"
fi
if [[ -z "$JAVA_BIN" || ! -x "$JAVA_BIN" ]]; then
    if ! command -v curl >/dev/null 2>&1 || ! command -v tar >/dev/null 2>&1; then
        printf 'Error: curl and tar are required to download and unpack Java.\n' >&2
        exit 1
    fi

    mkdir -p "$RUNTIME_ROOT"
    TEMP_DIR="$(mktemp -d "$RUNTIME_ROOT/.temurin-24-$OS-$ARCH.XXXXXX")"
    trap 'rm -rf "$TEMP_DIR"' EXIT

    JRE_URL="https://api.adoptium.net/v3/binary/latest/24/ga/$OS/$ARCH/jre/hotspot/normal/eclipse"
    printf 'Downloading the Java 24 JRE for %s/%s...\n' "$OS" "$ARCH"
    curl --fail --location --retry 3 --output "$TEMP_DIR/jre.tar.gz" "$JRE_URL"

    mkdir "$TEMP_DIR/extracted"
    tar -xzf "$TEMP_DIR/jre.tar.gz" -C "$TEMP_DIR/extracted"
    if [[ ! -x "$(find_java "$TEMP_DIR/extracted")" ]]; then
        printf 'Error: the downloaded archive did not contain a usable Java runtime.\n' >&2
        exit 1
    fi

    rm -rf "$RUNTIME_DIR"
    mv "$TEMP_DIR/extracted" "$RUNTIME_DIR"
    JAVA_BIN="$(find_java "$RUNTIME_DIR")"
fi

cd "$SCRIPT_DIR"
exec "$JAVA_BIN" --enable-preview -jar "$JAR_PATH" "$@"
