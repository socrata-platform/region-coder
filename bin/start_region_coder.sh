#!/bin/bash
# Start region-coder
BASEDIR=$(dirname $0)/..
JARFILE=$BASEDIR/target/scala-2.10/region-coder-assembly-*.jar
if [ ! -e $JARFILE ]; then
  cd $BASEDIR && sbt assembly
fi
java -jar $JARFILE &
