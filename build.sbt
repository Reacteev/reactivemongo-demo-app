name := "reactivemongo-demo-app"

val buildVersion = "0.13.0-play26"

version := buildVersion

resolvers += "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.4",
  "org.reactivemongo" %% "play2-reactivemongo" % buildVersion,
  "org.reactivemongo" %% "reactivemongo-akkastream" % "0.13.0"
)

routesGenerator := InjectedRoutesGenerator

fork in run := true

lazy val root = (project in file(".")).enablePlugins(PlayScala)
