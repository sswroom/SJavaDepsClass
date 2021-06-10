#!/bin/sh
#javadoc -d jdoc/ -sourcepath src/main/java -subpackages com.shared.util
export JAVA_HOME=/usr/lib/jvm/default-java
mvn javadoc:javadoc
