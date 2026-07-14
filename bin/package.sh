#!/bin/sh

if [ -z "$JAVA_HOME_25" ]; then
  echo "variável de ambiente JAVA_HOME_25 não definida"
  exit 1
fi

PATH=$JAVA_HOME_25/bin:$PATH

DMH_DIR=/home/javaapps/sbt-projects/DeCSMeSHHighlighter
JETTY_HOME=$DMH_DIR/jetty-home-11.0.14
APP_WAR=decsmeshfinder.war
APP_CONTEXT="/${APP_WAR%.war}"

cd $DMH_DIR/jetty-base || exit

"$JETTY_HOME/bin/jetty.sh" stop

cd $DMH_DIR || exit

sbt clean
sbt package

cd $DMH_DIR/jetty-base || exit

rm -f ../jetty-base/webapps/$APP_WAR
cp -L ../target/out/jvm/scala-3.3.8/decsmeshfinder/$APP_WAR ../jetty-base/webapps/$APP_WAR

"$JETTY_HOME/bin/jetty.sh" start

JETTY_PORT=$(awk -F= '/^[[:space:]]*jetty\.http\.port[[:space:]]*=/{gsub(/[[:space:]]/, "", $2); print $2; exit}' start.d/http.ini)
[ -n "$JETTY_PORT" ] || JETTY_PORT=8080

if grep -q "STARTED" jetty.state; then
  echo "Aplicacao em execucao: http://localhost:${JETTY_PORT}${APP_CONTEXT}/"
else
  echo "Jetty nao iniciou corretamente. Verifique jetty-base/logs."
  exit 1
fi

printf '\a'   # soar um bip
