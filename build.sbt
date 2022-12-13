name := "DeCSMeSHFinder"

version := "1.0"

scalaVersion := "2.13.10" //"2.13.4"

//val servletApiVersion = "4.0.1" //"3.0.1"
val jakartaServletApiVersion = "6.0.0"
val luceneVersion = "9.4.2" //"8.7.0"
val scalaTestVersion = "3.3.0-SNAP3" //"3.2.0-M2"

libraryDependencies ++= Seq(
  //"javax.servlet" % "javax.servlet-api" % servletApiVersion % "provided",
  "jakarta.servlet" % "jakarta.servlet-api" % jakartaServletApiVersion % "provided",
  "org.apache.lucene" % "lucene-core" % luceneVersion,
  //"org.apache.lucene" % "lucene-analyzers-common" % luceneVersion,
  "org.apache.lucene" % "lucene-backward-codecs" % luceneVersion,
  "org.scalactic" %% "scalactic" % scalaTestVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

Jetty / containerLibs := Seq("org.eclipse.jetty" % "jetty-runner" % "11.0.13")

Test / logBuffered := false
trapExit := false

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Ywarn-unused")

enablePlugins(JettyPlugin)

Jetty / javaOptions ++= Seq(
  "-Xdebug",
  "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
)

containerPort := 7181

assembly / assemblyMergeStrategy := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
