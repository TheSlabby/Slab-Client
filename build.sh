#!/bin/zsh

# Point directly to the Java 25 installed by SDKMAN!
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.3-tem"

./gradlew build

