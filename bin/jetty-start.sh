#!/bin/sh

DMH_DIR=/home/javaapps/sbt-projects/DeCSMeSHHighlighter

cd $DMH_DIR/jetty-base || exit

../jetty-home-11.0.14/bin/jetty.sh start
ret="$?"

sleep 120s

cd -

exit $ret
