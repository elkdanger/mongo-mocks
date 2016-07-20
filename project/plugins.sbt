logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.3")

// Bintray publishing
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

// Releasing
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

// SCoverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")