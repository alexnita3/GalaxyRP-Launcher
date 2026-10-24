#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_PATH="$SCRIPT_DIR/GalaxyRP Launcher.jar"

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

if [[ "$OS" == "mac" ]]; then
    INSTALL_DIR="/Library/Java/JavaVirtualMachines/temurin-24.jdk"
else
    INSTALL_DIR="/usr/local/lib/jvm/temurin-24"
fi

JAVA_BIN=""
SYSTEM_JAVA="$(command -v java 2>/dev/null || true)"
if [[ -n "$SYSTEM_JAVA" ]]; then
    SYSTEM_JAVA_VERSION="$("$SYSTEM_JAVA" -version 2>&1 | sed -nE '1{s/.*version "([0-9]+).*/\1/p;}' || true)"
    if [[ "$SYSTEM_JAVA_VERSION" =~ ^[0-9]+$ ]] && (( SYSTEM_JAVA_VERSION >= 24 )); then
        JAVA_BIN="$SYSTEM_JAVA"
        printf 'Using the system Java %s installation.\n' "$SYSTEM_JAVA_VERSION"
    fi
fi

find_java() {
    if [[ -d "$1" ]]; then
        find "$1" -path '*/bin/java' -print -quit 2>/dev/null
    fi
}

run_as_root() {
    if [[ "$(id -u)" -eq 0 ]]; then
        "$@"
    else
        sudo "$@"
    fi
}

if [[ -z "$JAVA_BIN" ]]; then
    if [[ "$OS" == "mac" ]]; then
        JAVA_BIN="$INSTALL_DIR/Contents/Home/bin/java"
    else
        JAVA_BIN="$INSTALL_DIR/bin/java"
    fi
fi
if [[ -z "$JAVA_BIN" || ! -x "$JAVA_BIN" ]]; then
    if ! command -v curl >/dev/null 2>&1 || ! command -v tar >/dev/null 2>&1; then
        printf 'Error: curl and tar are required to download and unpack Java 24.\n' >&2
        exit 1
    fi
    if [[ "$(id -u)" -ne 0 ]] && ! command -v sudo >/dev/null 2>&1; then
        printf 'Error: sudo is required to install Java 24 system-wide.\n' >&2
        exit 1
    fi

    TEMP_DIR="$(mktemp -d "${TMPDIR:-/tmp}/temurin-24-$OS-$ARCH.XXXXXX")"
    trap 'rm -rf "$TEMP_DIR"' EXIT

    JRE_URL="https://api.adoptium.net/v3/binary/latest/24/ga/$OS/$ARCH/jre/hotspot/normal/eclipse"
    printf 'Downloading the Java 24 JRE for %s/%s...\n' "$OS" "$ARCH"
    curl --fail --location --retry 3 --output "$TEMP_DIR/jre.tar.gz" "$JRE_URL"

    mkdir "$TEMP_DIR/extracted"
    tar -xzf "$TEMP_DIR/jre.tar.gz" -C "$TEMP_DIR/extracted"
    ARCHIVE_JAVA="$(find_java "$TEMP_DIR/extracted")"
    if [[ -z "$ARCHIVE_JAVA" || ! -x "$ARCHIVE_JAVA" ]]; then
        printf 'Error: the downloaded archive did not contain a usable Java runtime.\n' >&2
        exit 1
    fi

    ARCHIVE_JAVA_HOME="$(dirname "$(dirname "$ARCHIVE_JAVA")")"
    if [[ "$OS" == "mac" ]]; then
        ARCHIVE_CONTENTS="$(dirname "$ARCHIVE_JAVA_HOME")"
        if [[ ! -d "$ARCHIVE_CONTENTS" || "$(basename "$ARCHIVE_CONTENTS")" != "Contents" ]]; then
            printf 'Error: the downloaded macOS runtime has an unexpected layout.\n' >&2
            exit 1
        fi
        run_as_root rm -rf "$INSTALL_DIR"
        run_as_root mkdir -p "$(dirname "$INSTALL_DIR")"
        run_as_root mkdir -p "$INSTALL_DIR"
        run_as_root cp -R "$ARCHIVE_CONTENTS" "$INSTALL_DIR/Contents"
        JAVA_BIN="$INSTALL_DIR/Contents/Home/bin/java"
    else
        run_as_root rm -rf "$INSTALL_DIR"
        run_as_root mkdir -p "$INSTALL_DIR"
        run_as_root cp -R "$ARCHIVE_JAVA_HOME/." "$INSTALL_DIR/"
        JAVA_BIN="$INSTALL_DIR/bin/java"
        if command -v update-alternatives >/dev/null 2>&1; then
            run_as_root update-alternatives --install /usr/bin/java java "$JAVA_BIN" 2400
        elif command -v alternatives >/dev/null 2>&1; then
            run_as_root alternatives --install /usr/bin/java java "$JAVA_BIN" 2400
        elif ! command -v java >/dev/null 2>&1; then
            run_as_root ln -s "$JAVA_BIN" /usr/local/bin/java
        fi
    fi
fi

if [[ ! -x "$JAVA_BIN" ]]; then
    printf 'Error: Java could not be found at the expected installation path: %s\n' "$JAVA_BIN" >&2
    exit 1
fi

cd "$SCRIPT_DIR"
exec "$JAVA_BIN" --enable-preview -jar "$JAR_PATH" "$@"
