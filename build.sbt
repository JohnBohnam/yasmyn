ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "main"
  )

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.5.3",
  "com.typesafe.akka" %% "akka-stream" % "2.8.7",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3",
  "com.typesafe.slick" %% "slick" % "3.5.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.2",
  "org.xerial" % "sqlite-jdbc" % "3.46.1.2",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
  "com.auth0" % "java-jwt" % "4.2.1",
  "ch.qos.logback" % "logback-classic" % "1.2.11",
)
libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.1.3"

