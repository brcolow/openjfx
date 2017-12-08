#!/usr/bin/env bash
set -uo pipefail

echo "which java: $(which java)"
ulimit -c unlimited -S

./gradlew build

# Print core dumps when JVM crashes.
RESULT=$?
echo "Exit code from gradle build: $RESULT"

if [[ ${RESULT} -ne 0 ]]; then
  printf '\n\n======== JVM CRASH LOGS ======='

  FIND="gfind" && [[ "${TRAVIS_OS_NAME}" == osx ]] && FIND="find"

  ${FIND} . -name "hs_err_pid*.log" -type f -printf '\n%p\n' -exec cat {} \;

  CORES=''
  if [[ "${TRAVIS_OS_NAME}" == osx ]]; then
    CORES="$(find /cores/ -type f -print)"
  else
    CORES="$(find . -type f -regex '*core.[0-9]{6}' -print)"
  fi

  if [ -n "${CORES}" ]; then
    for core in ${CORES}; do
    printf '\n\n======= Core file %s =======' "$core"
    if [[ "${TRAVIS_OS_NAME}" == osx ]]; then
      lldb -Q -o "bt all" -f "$(which java)" -c "${core}"
    else
      gdb -n -batch -ex "thread apply all bt" -ex "set pagination 0" "$(which java)" -c "${core}"
    fi
  done
  fi

  exit ${RESULT}
fi

# vim :set ts=2 sw=2 sts=2 et:
