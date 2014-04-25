#!/bin/bash

$JAVA_HOME/bin/javac -classpath ~/hadoop/hadoop-1.2.1/hadoop-core-1.2.1.jar -d classes src/edu/sjsu/cs267/tools/*.java
$JAVA_HOME/bin/javac -classpath ~/hadoop/hadoop-1.2.1/hadoop-core-1.2.1.jar:classes -d classes src/edu/sjsu/cs267/*.java
$JAVA_HOME/bin/jar -cvf ada_boost_mr.jar -C classes/ .

