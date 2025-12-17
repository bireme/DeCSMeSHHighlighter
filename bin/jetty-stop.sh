#!/bin/sh

if [ -z "$JAVA_HOME_25" ]; then
  echo "variável de ambiente JAVA_HOME_25 não definida"
  exit 1
fi

PATH=$JAVA_HOME_25/bin:$PATH

DMH_DIR=/home/javaapps/sbt-projects/DeCSMeSHHighlighter

cd $DMH_DIR/jetty-base || exit

../jetty-home-11.0.14/bin/jetty.sh stop
ret="$?"

sleep 120s

cd -

exit $ret
