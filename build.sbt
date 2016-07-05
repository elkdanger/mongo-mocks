name := "mongo-mocks"

version := "0.0.6-SNAPSHOT"

organization := "org.elkdanger"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.13",
  "org.scalatest" %% "scalatest" % "2.2.6",
  "org.mockito" % "mockito-core" % "1.9.5"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)