# DO NOT EDIT - See: https://www.eclipse.org/jetty/documentation/current/startup-modules.html

[description]
Enables GCloud Datastore API and implementation.

[tags]
3rdparty
gcloud

[depends]
gcloud
logging-jcl-capture
logging-jul-capture


[files]
maven://com.fasterxml.jackson.core/jackson-core/2.14.1|lib/gcloud/jackson-core-2.14.1.jar
maven://com.google.android/annotations/4.1.1.4|lib/gcloud/annotations-4.1.1.4.jar
maven://com.google.api-client/google-api-client/2.0.0|lib/gcloud/google-api-client-2.0.0.jar
maven://com.google.api.grpc/grpc-google-cloud-datastore-admin-v1/2.11.4|lib/gcloud/grpc-google-cloud-datastore-admin-v1-2.11.4.jar
maven://com.google.api.grpc/proto-google-cloud-datastore-admin-v1/2.11.4|lib/gcloud/proto-google-cloud-datastore-admin-v1-2.11.4.jar
maven://com.google.api.grpc/proto-google-cloud-datastore-v1/0.102.4|lib/gcloud/proto-google-cloud-datastore-v1-0.102.4.jar
maven://com.google.api.grpc/proto-google-common-protos/2.9.2|lib/gcloud/proto-google-common-protos-2.9.2.jar
maven://com.google.api.grpc/proto-google-iam-v1/1.5.2|lib/gcloud/proto-google-iam-v1-1.5.2.jar
maven://com.google.api/api-common/2.2.1|lib/gcloud/api-common-2.2.1.jar
maven://com.google.api/gax-grpc/2.19.1|lib/gcloud/gax-grpc-2.19.1.jar
maven://com.google.api/gax-httpjson/0.104.1|lib/gcloud/gax-httpjson-0.104.1.jar
maven://com.google.api/gax/2.19.1|lib/gcloud/gax-2.19.1.jar
maven://com.google.auth/google-auth-library-credentials/1.11.0|lib/gcloud/google-auth-library-credentials-1.11.0.jar
maven://com.google.auth/google-auth-library-oauth2-http/1.11.0|lib/gcloud/google-auth-library-oauth2-http-1.11.0.jar
maven://com.google.auto.value/auto-value-annotations/1.9|lib/gcloud/auto-value-annotations-1.9.jar
maven://com.google.cloud.datastore/datastore-v1-proto-client/2.11.4|lib/gcloud/datastore-v1-proto-client-2.11.4.jar
maven://com.google.cloud/google-cloud-core-http/2.8.12|lib/gcloud/google-cloud-core-http-2.8.12.jar
maven://com.google.cloud/google-cloud-core/2.8.12|lib/gcloud/google-cloud-core-2.8.12.jar
maven://com.google.cloud/google-cloud-datastore/2.11.4|lib/gcloud/google-cloud-datastore-2.11.4.jar
maven://com.google.code.findbugs/jsr305/3.0.2|lib/gcloud/jsr305-3.0.2.jar
maven://com.google.code.gson/gson/2.9.1|lib/gcloud/gson-2.9.1.jar
maven://com.google.errorprone/error_prone_annotations/2.15.0|lib/gcloud/error_prone_annotations-2.15.0.jar
maven://com.google.guava/failureaccess/1.0.1|lib/gcloud/failureaccess-1.0.1.jar
maven://com.google.guava/guava/31.1-jre|lib/gcloud/guava-31.1-jre.jar
maven://com.google.guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava|lib/gcloud/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar
maven://com.google.http-client/google-http-client-apache-v2/1.42.2|lib/gcloud/google-http-client-apache-v2-1.42.2.jar
maven://com.google.http-client/google-http-client-appengine/1.42.2|lib/gcloud/google-http-client-appengine-1.42.2.jar
maven://com.google.http-client/google-http-client-gson/1.42.2|lib/gcloud/google-http-client-gson-1.42.2.jar
maven://com.google.http-client/google-http-client-jackson2/1.42.2|lib/gcloud/google-http-client-jackson2-1.42.2.jar
maven://com.google.http-client/google-http-client-protobuf/1.42.2|lib/gcloud/google-http-client-protobuf-1.42.2.jar
maven://com.google.http-client/google-http-client/1.42.2|lib/gcloud/google-http-client-1.42.2.jar
maven://com.google.j2objc/j2objc-annotations/1.3|lib/gcloud/j2objc-annotations-1.3.jar
maven://com.google.oauth-client/google-oauth-client/1.34.1|lib/gcloud/google-oauth-client-1.34.1.jar
maven://com.google.protobuf/protobuf-java-util/3.21.6|lib/gcloud/protobuf-java-util-3.21.6.jar
maven://com.google.protobuf/protobuf-java/3.21.6|lib/gcloud/protobuf-java-3.21.6.jar
maven://com.google.re2j/re2j/1.6|lib/gcloud/re2j-1.6.jar
maven://commons-codec/commons-codec/1.15|lib/gcloud/commons-codec-1.15.jar
maven://commons-logging/commons-logging/1.2|lib/gcloud/commons-logging-1.2.jar
maven://io.grpc/grpc-alts/1.49.0|lib/gcloud/grpc-alts-1.49.0.jar
maven://io.grpc/grpc-api/1.49.0|lib/gcloud/grpc-api-1.49.0.jar
maven://io.grpc/grpc-auth/1.49.0|lib/gcloud/grpc-auth-1.49.0.jar
maven://io.grpc/grpc-context/1.49.0|lib/gcloud/grpc-context-1.49.0.jar
maven://io.grpc/grpc-core/1.49.2|lib/gcloud/grpc-core-1.49.2.jar
maven://io.grpc/grpc-googleapis/1.49.0|lib/gcloud/grpc-googleapis-1.49.0.jar
maven://io.grpc/grpc-grpclb/1.49.0|lib/gcloud/grpc-grpclb-1.49.0.jar
maven://io.grpc/grpc-netty-shaded/1.49.0|lib/gcloud/grpc-netty-shaded-1.49.0.jar
maven://io.grpc/grpc-protobuf-lite/1.49.0|lib/gcloud/grpc-protobuf-lite-1.49.0.jar
maven://io.grpc/grpc-protobuf/1.49.0|lib/gcloud/grpc-protobuf-1.49.0.jar
maven://io.grpc/grpc-services/1.49.0|lib/gcloud/grpc-services-1.49.0.jar
maven://io.grpc/grpc-stub/1.49.0|lib/gcloud/grpc-stub-1.49.0.jar
maven://io.grpc/grpc-xds/1.49.0|lib/gcloud/grpc-xds-1.49.0.jar
maven://io.opencensus/opencensus-api/0.31.1|lib/gcloud/opencensus-api-0.31.1.jar
maven://io.opencensus/opencensus-contrib-http-util/0.31.1|lib/gcloud/opencensus-contrib-http-util-0.31.1.jar
maven://io.opencensus/opencensus-proto/0.2.0|lib/gcloud/opencensus-proto-0.2.0.jar
maven://io.perfmark/perfmark-api/0.25.0|lib/gcloud/perfmark-api-0.25.0.jar
maven://org.apache.httpcomponents/httpclient/4.5.13|lib/gcloud/httpclient-4.5.13.jar
maven://org.apache.httpcomponents/httpcore/4.4.15|lib/gcloud/httpcore-4.4.15.jar
maven://org.checkerframework/checker-qual/3.25.0|lib/gcloud/checker-qual-3.25.0.jar
maven://org.codehaus.mojo/animal-sniffer-annotations/1.22|lib/gcloud/animal-sniffer-annotations-1.22.jar
maven://org.conscrypt/conscrypt-openjdk-uber/2.5.2|lib/gcloud/conscrypt-openjdk-uber-2.5.2.jar
maven://org.slf4j/slf4j-api/2.0.5|lib/gcloud/slf4j-api-2.0.5.jar
maven://org.threeten/threetenbp/1.6.1|lib/gcloud/threetenbp-1.6.1.jar

