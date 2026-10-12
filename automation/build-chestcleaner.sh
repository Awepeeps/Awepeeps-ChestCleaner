#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE="maven:3.9.9-eclipse-temurin-21@sha256:3a4ab3276a087bf276f79cae96b1af04f53731bec53fb2e651aca79e4b10211e"

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
    cp target/chestcleaner-2.4.5-awepeeps.1.jar /workspace/target/
  '

printf 'Built %s\n' "$ROOT/target/chestcleaner-2.4.5-awepeeps.1.jar"
