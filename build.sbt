name := "DeCSMeSHFinder"

version := "1.0"

scalaVersion := "2.13.10" //"2.13.4"

val jakartaServletApiVersion = "6.0.0"
val jakartaWsRsVersion= "3.1.0"
val luceneVersion = "9.6.0" //"9.4.2"
val scalaTestVersion = "3.3.0-SNAP3" //"3.2.0-M2"

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
