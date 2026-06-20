#!/bin/zsh

# Point directly to the Java 8 installed by SDKMAN!
export JAVA_HOME="$HOME/.sdkman/candidates/java/8.0.412-tem"

./gradlew build

