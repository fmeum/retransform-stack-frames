#!/usr/bin/env sh
mvn compile assembly:single
java --version
java -javaagent:target/retransform-stack-frames-1.0-SNAPSHOT-jar-with-dependencies.jar -jar target/retransform-stack-frames-1.0-SNAPSHOT-jar-with-dependencies.jar
