#!/bin/sh
# Maven Wrapper script
# https://maven.apache.org/wrapper/

set -e

# Determine the project base directory
if [ -z "$MAVEN_PROJECTBASEDIR" ]; then
  MAVEN_PROJECTBASEDIR=$(cd "$(dirname "$0")" && pwd)
  export MAVEN_PROJECTBASEDIR
fi

WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Determine Maven home
if [ -n "$M2_HOME" ]; then
  MAVEN_HOME="$M2_HOME"
elif [ -n "$MAVEN_HOME" ]; then
  MAVEN_HOME="$MAVEN_HOME"
else
  MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.9"
fi

MAVEN_CMD="$MAVEN_HOME/bin/mvn"

# If Maven is not installed in wrapper location, download it
if [ ! -f "$MAVEN_CMD" ]; then
  # Read distribution URL from properties
  if [ -f "$WRAPPER_PROPERTIES" ]; then
    DIST_URL=$(grep "distributionUrl" "$WRAPPER_PROPERTIES" | cut -d'=' -f2-)
  else
    DIST_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"
  fi

  echo "Downloading Maven from $DIST_URL"
  DOWNLOAD_DIR="$HOME/.m2/wrapper/dists"
  mkdir -p "$DOWNLOAD_DIR"

  if command -v curl > /dev/null 2>&1; then
    curl -fsSL "$DIST_URL" -o "$DOWNLOAD_DIR/maven.zip"
  elif command -v wget > /dev/null 2>&1; then
    wget -q "$DIST_URL" -O "$DOWNLOAD_DIR/maven.zip"
  else
    echo "Error: Neither curl nor wget found. Cannot download Maven."
    exit 1
  fi

  unzip -qo "$DOWNLOAD_DIR/maven.zip" -d "$DOWNLOAD_DIR"
  rm "$DOWNLOAD_DIR/maven.zip"
  chmod +x "$MAVEN_HOME/bin/mvn"
fi

# Fall back to system Maven if wrapper Maven doesn't exist
if [ ! -f "$MAVEN_CMD" ]; then
  if command -v mvn > /dev/null 2>&1; then
    MAVEN_CMD="mvn"
  else
    echo "Error: Maven not found. Please install Maven or ensure the wrapper can download it."
    exit 1
  fi
fi

exec "$MAVEN_CMD" \
  $MAVEN_OPTS \
  $MAVEN_DEBUG_OPTS \
  -f "$MAVEN_PROJECTBASEDIR/pom.xml" \
  "$@"
