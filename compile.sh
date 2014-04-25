#!/bin/bash

javac -classpath lib/hadoop-core-1.2.1.jar -d classes src/edu/sjsu/cs267/tools/*.java
javac -classpath lib/hadoop-core-1.2.1.jar:classes -d classes src/edu/sjsu/cs267/*.java
jar -cvf adaboost_mr.jar -C classes/ .

