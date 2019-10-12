#!/bin/sh

cd "$(dirname "$0")/"

OPTIONS=" -Xmx1500m -Xdock:name=KeysAnalyzer -XstartOnFirstThread "
CLASSPATH="lib/keysanalyzer.jar:lib/dtd.jar:lib/json.jar:lib/jsoup-1.11.3.jar:lib/mapdb.jar:lib/openxliff.jar:lib/mac64/swt.jar"

java ${OPTIONS} -cp ${CLASSPATH} com.maxprograms.keysanalyzer.KeysAnalyzer