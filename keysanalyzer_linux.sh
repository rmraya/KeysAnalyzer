#!/bin/bash

cd "$(dirname "$0")/"

OPTIONS=" -Xmx1500m"
CLASSPATH="lib/keysanalyzer.jar:lib/mapdb.jar:lib/openxliff.jar:lib/gtk64/swt.jar"


java ${OPTIONS} -cp ${CLASSPATH} com.maxprograms.keysanalyzer.KeysAnalyzer