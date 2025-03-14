#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-17.0.14.0.7-3.el8.x86_64
export PATH=$JAVA_HOME/bin:$PATH
./mvnw "$@"
