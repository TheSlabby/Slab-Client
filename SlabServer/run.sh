#!/bin/bash
set -e

mkdir -p build
cd build
cmake ..
make -j24
cd ..
build/slabserver "$@"
