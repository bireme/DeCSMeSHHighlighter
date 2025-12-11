#!/bin/sh

DMH_DIR=/home/javaapps/sbt-projects/DeCSMeSHHighlighter

cd $DMH_DIR/jetty-base || exit

../jetty-home-11.0.14/bin/jetty.sh stop

cd $DMH_DIR || exit

sbt clean package

cd $DMH_DIR/jetty-base || exit

mv ../target/scala-3.3.7/decsmeshfinder.war ../jetty-base/webapps

../jetty-home-11.0.14/bin/jetty.sh start

echo -e '\a'   # soar um bip
