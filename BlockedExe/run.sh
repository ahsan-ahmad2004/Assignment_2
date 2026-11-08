#!/bin/sh
# Compile and run BLOCKED.exe (needs JDK 11+)
mkdir -p out
javac -encoding UTF-8 -d out src/*.java && java -cp out Main
