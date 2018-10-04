name := "reactivemongo-demo-app"

version := "1.0.0"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.4",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.13.0-play26",
  "org.reactivemongo" %% "reactivemongo-akkastream" % "0.13.0"
)

routesGenerator := InjectedRoutesGenerator

fork in run := true

lazy val root = (project in file(".")).enablePlugins(PlayScala)

