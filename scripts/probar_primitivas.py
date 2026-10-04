"""Compila y prueba sólo las primitivas, sin depender del esqueleto incompleto."""
import argparse
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--java-home', required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
java_bin = Path(args.java_home) / 'bin'
sources = sorted((root / 'src/main/java/com/avilaos/core/structures').glob('*.java'))
sources.append(root / 'src/test/java/com/avilaos/core/structures/PrimitivasTest.java')
with tempfile.TemporaryDirectory(prefix='avilaos-primitivas-test-') as temp:
    subprocess.run([str(java_bin / 'javac'), '-Xlint:unchecked', '-Werror', '-d', temp,
                    *map(str, sources)], check=True)
    subprocess.run([str(java_bin / 'java'), '-cp', temp,
                    'com.avilaos.core.structures.PrimitivasTest'], check=True, timeout=15)
