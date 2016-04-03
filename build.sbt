name := "RedmineForSpigot"

version := "1.0.1"

scalaVersion := "2.11.8"

scalacOptions ++= List("-Ybackend:GenBCode", "-Ydelambdafy:method", "-target:jvm-1.8")

resolvers ++= Seq (
  "BungeeCord" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Spigot" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
)

libraryDependencies ++= Seq (
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.5.0",
  "org.spigotmc" % "spigot-api" % "1.9-R0.1-SNAPSHOT" % "provided",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.slf4j" % "slf4j-api" % "1.7.18",
  "org.slf4j" % "slf4j-nop" % "1.7.18",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4"
)