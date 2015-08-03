name := """HMonitor-DB"""

version := "1.0.0"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.11.6", scalaVersion.value)

libraryDependencies ++= Seq(
  "org.virtuslab" %% "unicorn-play" % "0.6.3",
  "com.h2database" % "h2" % "1.4.181" % "test"
  // Add your own project dependencies in the form:
  // "group" % "artifact" % "version"
)

lazy val `hmonitor-db` = (project in file(".")).enablePlugins(PlayScala, SbtWeb)


fork in run := false