name := "mongo-mocks"

version := "0.1.0"

organization := "org.elkdanger"

scalaVersion := "2.11.8"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

bintrayPackageLabels := Seq("testing", "mongo", "mock", "mockito", "play", "playframework")

publishMavenStyle := false

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.13",
  "org.scalatest" %% "scalatest" % "2.2.6",
  "org.mockito" % "mockito-core" % "1.9.5"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

// Coverage
coverageMinimum := 80
coverageFailOnMinimum := true
