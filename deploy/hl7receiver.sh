#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-8-oracle/jre
export CONFIG_JDBC_USERNAME=endeavour
export CONFIG_JDBC_PASSWORD=
export CONFIG_JDBC_URL=jdbc:postgresql://127.0.0.1/config

java -jar /opt/hl7receiver/hl7receiver-1.0-SNAPSHOT-jar-with-dependencies.jar -Xmx4g
