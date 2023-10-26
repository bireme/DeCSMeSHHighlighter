name := "DeCSMeSHFinder"

version := "1.0"

scalaVersion := "2.13.12" //"2.13.11"

val jakartaServletApiVersion = "6.0.0"
val jakartaWsRsVersion= "3.1.0"
val luceneVersion = "9.8.0" //"9.7.0"
val scalaTestVersion = "3.3.0-SNAP4" //"3.2.0-M2"

libraryDependencies ++= Seq(
  "jakarta.servlet" % "jakarta.servlet-api" % jakartaServletApiVersion % "provided",
  "jakarta.ws.rs" % "jakarta.ws.rs-api" % jakartaWsRsVersion,
  "org.apache.lucene" % "lucene-core" % luceneVersion,
  "org.apache.lucene" % "lucene-backward-codecs" % luceneVersion,
  "org.scalactic" %% "scalactic" % scalaTestVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

Test / logBuffered := false
trapExit := false

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Ywarn-unused")

enablePlugins(JettyPlugin)

assembly / assemblyMergeStrategy := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
