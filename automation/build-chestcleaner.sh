#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE="maven:3.9.16-eclipse-temurin-25@sha256:93b8a14ea2f412782e4e842651273b4d903e35cc496284f178fbbe2d67d00976"

docker run --rm \
  --user "$(id -u):$(id -g)" \
  -e MAVEN_CONFIG=/tmp/maven \
  -e MAVEN_OPTS=-Duser.home=/tmp \
  -v "$ROOT:/workspace" \
  -w /workspace \
  "$IMAGE" \
  sh -ec '
    build_dir="$(mktemp -d /tmp/chestcleaner-build.XXXXXX)"
    trap "rm -rf -- $build_dir" EXIT
    cp -R /workspace/. "$build_dir/"
    find "$build_dir" -type d -exec chmod 0755 {} +
    find "$build_dir" -type f -exec chmod 0644 {} +
    cd "$build_dir"
    mvn --batch-mode --no-transfer-progress clean verify
    mkdir -p /workspace/target
    cp target/chestcleaner-2.4.5-awepeeps.3.jar /workspace/target/
  '

printf 'Built %s\n' "$ROOT/target/chestcleaner-2.4.5-awepeeps.3.jar"
