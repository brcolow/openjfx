#!/usr/bin/env bash
set -euo pipefail

if [[ "${TRAVIS_OS_NAME}" == osx ]]; then
  brew update
  if [[ "${TRAVIS_JAVA_VERSION}" == 8 ]]; then
    brew cask reinstall caskroom/versions/java8
  elif [[ "${TRAVIS_JAVA_VERSION}" == 9 ]]; then
    brew cask reinstall java
  else
      echo "TRAVIS_JAVA_VERSION environment variable not set!"
  fi

  brew outdated gradle || brew upgrade gradle
  brew unlink python # fixes 'run_one_line' is not defined error in backtrace
fi
