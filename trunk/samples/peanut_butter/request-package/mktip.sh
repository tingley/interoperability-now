#!/usr/bin/env bash

# Build the tip package (unencrypted) in build/

set -e -x

DIR=`dirname $0`
BUILD_DIR=$DIR/build

if [ -d "$BUILD_DIR" ]; then
    rm -Rf $BUILD_DIR/*
else
    mkdir $BUILD_DIR
fi
cd $BUILD_DIR
cp -a ../reference .
cp -a ../bilingual .
cp -a ../preview .
cp -a ../sts .
find . -name \.svn|xargs rm -Rf
zip -r resources.zip reference bilingual preview sts
cp ../manifest.xml .
zip -r sample-request.tipp manifest.xml resources.zip
