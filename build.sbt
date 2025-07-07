name := "DeCSMeSHFinder"

version := "1.0"

scalaVersion := "2.13.16" //"2.13.13"

val jakartaServletApiVersion = "6.1.0" //"6.0.0"
val jakartaWsRsVersion= "4.0.0" //"3.1.0"
val luceneVersion = "9.12.1" //"9.8.0"
val tikaVersion = "3.2.0"
val sttpVersion = "4.0.9" //4.0.0-RC2"
val scalaTagsVersion = "0.13.1"
val scalaTestVersion = "3.3.0-SNAP4" //"3.2.0-M2"
val circeVersion = "0.14.14"
val jsoupVersion = "1.21.1"
val linguaVersion = "1.2.2"
val ollama4jVersion = "1.0.100"
val playJsonVersion = "2.10.7"

libraryDependencies ++= Seq(
  "jakarta.servlet" % "jakarta.servlet-api" % jakartaServletApiVersion % "provided",
  "jakarta.ws.rs" % "jakarta.ws.rs-api" % jakartaWsRsVersion,
  "org.apache.lucene" % "lucene-core" % luceneVersion,
  "org.apache.lucene" % "lucene-queryparser" % luceneVersion,
  "org.apache.lucene" % "lucene-backward-codecs" % luceneVersion,
  "org.apache.lucene" % "lucene-analysis-common" % luceneVersion,
  //"org.apache.tika" % "tika-core" % tikaVersion,
  //"org.apache.tika" % "tika-langdetect" % tikaVersion pomOnly(),
  "com.softwaremill.sttp.client4" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client4" %% "circe" % sttpVersion,
  "com.lihaoyi" %% "scalatags" % scalaTagsVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "org.jsoup" % "jsoup" % jsoupVersion,
  "io.github.ollama4j" % "ollama4j" % ollama4jVersion,
  "com.github.pemistahl" % "lingua" % linguaVersion,
  "com.typesafe.play" %% "play-json" % playJsonVersion,
  "org.scalactic" %% "scalactic" % scalaTestVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
)

Test / logBuffered := false
trapExit := false

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Ywarn-unused")

//enablePlugins(JettyPlugin)
//enablePlugins(WarPlugin)
enablePlugins(SbtWar)

assembly / assemblyMergeStrategy := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

artifactName := { (v: ScalaVersion, m: ModuleID, a: Artifact) =>
  "decsmeshfinder.war"
}

/*
// Configura o nome do arquivo .jar de maneira explícita
artifactName in (Compile, packageBin) := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "meu-jar-nome-" + version.value + ".jar"
}

// Configura o nome do arquivo .war de maneira explícita
artifactName in (Compile, packageWar) := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "meu-war-nome-" + version.value + ".war"
}*/
