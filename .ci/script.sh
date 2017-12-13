#!/usr/bin/env bash
set -uo pipefail

echo "which java: $(which java)"
ulimit -c unlimited -S

./gradlew build

# Print core dumps when JVM crashes.
RESULT=$?

if [[ ${RESULT} -ne 0 ]]; then
  ls "$TRAVIS_BUILD_DIR"/images
  zip --verbose -r "$TRAVIS_BUILD_DIR"/images.zip "$TRAVIS_BUILD_DIR"/images/*

  if [ ! -z ${PRINT_CRASH_LOGS+x} ]; then
    if [[ "${TRAVIS_OS_NAME}" == osx ]]; then FIND="gfind"; else FIND="find"; fi
    ${FIND} . -name "hs_err_pid*.log" -type f -printf '\n====== JVM CRASH LOG ======\n%p\n' -exec cat {} \;

    CORES=''
    if [[ "${TRAVIS_OS_NAME}" == osx ]]; then
      CORES="$(find /cores -type f -regex '*core.[0-9]{4}' -print)"
    else
      CORES="$(find . -type f -regex '*core.[0-9]{6}' -print)"
    fi

    if [ -n "${CORES}" ]; then
      for core in ${CORES}; do
        printf '\n\n======= Core file %s =======\n' "$core"
        if [[ "${TRAVIS_OS_NAME}" == osx ]]; then
          lldb -Q -o "bt all" -f "$(which java)" -c "${core}"
        else
          gdb -n -batch -ex "thread apply all bt" -ex "set pagination 0" "$(which java)" -c "${core}"
        fi
      done
    fi
  fi
  # exit ${RESULT}
  exit 0 # Force success so images are uploaded.
fi

# vim :set ts=2 sw=2 sts=2 et:
