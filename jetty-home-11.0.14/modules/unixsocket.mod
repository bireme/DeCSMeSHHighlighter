# DO NOT EDIT - See: https://www.eclipse.org/jetty/documentation/current/startup-modules.html

[description]
Enables a Unix Domain Socket Connector.
The connector can receive requests from a local proxy and/or SSL offloader (eg haproxy) in either
HTTP or TCP mode.  Unix Domain Sockets are more efficient than localhost TCP/IP connections
as they reduce data copies, avoid needless fragmentation and have better dispatch behaviours.
When enabled with corresponding support modules, the connector can 
accept HTTP, HTTPS or HTTP2C traffic.

[deprecated]
Module 'unixsocket' is deprecated for removal.
Use 'unixdomain-http' instead (requires Java 16 or later).

[tags]
connector
deprecated

[depend]
server

[xml]
etc/jetty-unixsocket.xml

[lib]
lib/jetty-unixsocket-common-${jetty.version}.jar
lib/jetty-unixsocket-server-${jetty.version}.jar
lib/jnr/*.jar



[files]
maven://com.github.jnr/jffi/1.3.9|lib/jnr/jffi-1.3.9.jar
maven://com.github.jnr/jffi/1.3.9/jar/native|lib/jnr/jffi-1.3.9-native.jar
maven://com.github.jnr/jnr-constants/0.10.4|lib/jnr/jnr-constants-0.10.4.jar
maven://com.github.jnr/jnr-enxio/0.32.13|lib/jnr/jnr-enxio-0.32.13.jar
maven://com.github.jnr/jnr-ffi/2.2.12|lib/jnr/jnr-ffi-2.2.12.jar
maven://com.github.jnr/jnr-posix/3.1.15|lib/jnr/jnr-posix-3.1.15.jar
maven://com.github.jnr/jnr-unixsocket/0.38.17|lib/jnr/jnr-unixsocket-0.38.17.jar
maven://com.github.jnr/jnr-x86asm/1.0.2|lib/jnr/jnr-x86asm-1.0.2.jar
maven://org.ow2.asm/asm-analysis/9.4|lib/jnr/asm-analysis-9.4.jar
maven://org.ow2.asm/asm-commons/9.4|lib/jnr/asm-commons-9.4.jar
maven://org.ow2.asm/asm-tree/9.4|lib/jnr/asm-tree-9.4.jar
maven://org.ow2.asm/asm-util/9.4|lib/jnr/asm-util-9.4.jar
maven://org.ow2.asm/asm/9.4|lib/jnr/asm-9.4.jar

[license]
Jetty UnixSockets is implemented using the Java Native Runtime, which is an
open source project hosted on Github and released under the Apache 2.0 license.
https://github.com/jnr/jnr-unixsocket
http://www.apache.org/licenses/LICENSE-2.0.html

[ini-template]
### Unix SocketHTTP Connector Configuration

## Unix socket path to bind to
# jetty.unixsocket.path=/tmp/jetty.sock

## Connector idle timeout in milliseconds
# jetty.unixsocket.idleTimeout=30000

## Number of selectors (-1 picks default)
# jetty.unixsocket.selectors=-1

## ServerSocketChannel backlog (0 picks platform default)
# jetty.unixsocket.acceptQueueSize=0
