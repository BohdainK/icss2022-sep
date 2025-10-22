#!/usr/bin/env bash
# set-java13.sh
# Source this file to set JAVA_HOME and PATH for this project only:
#   source set-java13.sh

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 13 2>/dev/null || true)
fi

if [ -n "$JAVA_HOME" ]; then
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "JAVA_HOME set to $JAVA_HOME"
else
  echo "Warning: Java 13 not found via /usr/libexec/java_home -v 13"
fi
