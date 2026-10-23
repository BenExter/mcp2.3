#!/usr/bin/env bash
set -euo pipefail
truchosafio_jdk=/workspace/toolchains/jdk21/usr/lib/jvm/java-21-openjdk-amd64
if [[ -x "$truchosafio_jdk/bin/javac" ]]; then "$truchosafio_jdk/bin/javac" -version; exit 0; fi
mkdir -p /workspace/toolchains
python3 - <<'PY'
import pathlib,subprocess,hashlib
packages=[
 ('openjdk-21-jdk-headless','f3abafb6c644b03df042824e707cd211ea33254761d7f7b75be3f8dc0df97c7a'),
 ('openjdk-21-jre-headless','e95f36193e45464ac758e5436f940bf3eaecc77de67188275a42104f58aa7674')]
for name,expected in packages:
 target=pathlib.Path('/workspace/toolchains')/(name+'.deb')
 if not target.exists() or hashlib.sha256(target.read_bytes()).hexdigest()!=expected:
  url=f'https://deb.debian.org/debian/pool/main/o/openjdk-21/{name}_21.0.12.1+1-1~deb13u1_amd64.deb'
  subprocess.run(['curl','--fail','--location','--max-time','180',url,'--output',str(target)],check=True)
 if hashlib.sha256(target.read_bytes()).hexdigest()!=expected:raise RuntimeError('JDK checksum mismatch')
 subprocess.run(['dpkg-deb','-x',str(target),'/workspace/toolchains/jdk21'],check=True)
PY
"$truchosafio_jdk/bin/javac" -version
