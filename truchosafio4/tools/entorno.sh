#!/usr/bin/env bash
# Source this file from the project root. No secrets are stored here.
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-/workspace/.gradle}"
if [[ -x /workspace/toolchains/jdk21/usr/lib/jvm/java-21-openjdk-amd64/bin/javac ]]; then
  export JAVA_HOME=/workspace/toolchains/jdk21/usr/lib/jvm/java-21-openjdk-amd64
  export PATH="$JAVA_HOME/bin:$PATH"
fi
if [[ -n "${HTTPS_PROXY:-}" ]]; then
  truchosafio_proxy_args=$(python3 - <<'PY'
import os,urllib.parse
u=urllib.parse.urlsplit(os.environ['HTTPS_PROXY'])
if u.hostname and u.port:
 print(f'-Dhttps.proxyHost={u.hostname} -Dhttps.proxyPort={u.port} -Dhttp.proxyHost={u.hostname} -Dhttp.proxyPort={u.port}')
PY
)
  export GRADLE_OPTS="${GRADLE_OPTS:-} $truchosafio_proxy_args"
  unset truchosafio_proxy_args
fi
