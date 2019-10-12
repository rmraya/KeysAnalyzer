@echo off

set CLASSPATH="lib\keyanalyzer.jar;lib\dtd.jar;lib\json.jar;lib\jsoup-1.11.3.jar;lib\mapdb.jar;lib\openxliff.jar;lib\win64\swt.jar"

start javaw.exe  -cp %CLASSPATH% com.maxprograms.keyanalyzer.KeyAnalyzer


