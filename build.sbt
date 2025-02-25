ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "conduktor-kafka"
  )

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "3.5.1",
  "ch.qos.logback" % "logback-classic" % "1.5.16",
  "com.typesafe.play" %% "play-json" % "2.10.6",
  "com.twitter" %% "finagle-http" % "24.2.0"
)

resolvers += Resolver.mavenCentral