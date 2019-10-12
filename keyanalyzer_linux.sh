#!/bin/bash

cd "$(dirname "$0")/"

OPTIONS=" -Xmx1500m"
CLASSPATH="lib/keyanalyzer.jar:lib/dtd.jar:lib/json.jar:lib/jsoup-1.11.3.jar:lib/mapdb.jar:lib/openxliff.jar:lib/gtk64/swt.jar"


java ${OPTIONS} -cp ${CLASSPATH} com.maxprograms.keyanalyzer.KeyAnalyzer