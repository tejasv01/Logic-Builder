#!/bin/bash
set -e

# Detect JAVA_HOME if not already set
if [ -z "$JAVA_HOME" ]; then
    if [ -x "/usr/libexec/java_home" ]; then
        JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null || echo "")
    fi
    if [ -z "$JAVA_HOME" ] && [ -d "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home" ]; then
        JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    export JAVA_HOME
    export PATH="$JAVA_HOME/bin:$PATH"
fi

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

# Build if out directory or JAR doesn't exist
if [ ! -f "logic-circuit-simulator.jar" ]; then
    echo "Compiling..."
    mkdir -p out
    javac --release 17 -d out $(find src/main/java -name "*.java")
    jar cfe logic-circuit-simulator.jar circuitsim.Main -C out .
fi

echo "Launching Logic Circuit Simulator..."
java -jar logic-circuit-simulator.jar "$@"
