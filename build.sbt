ThisBuild / scalaVersion := "3.3.3"

val http4sVersion = "0.23.30"
val munitVersion  = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "Otus-Scala-Final",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "ch.qos.logback" % "logback-classic"  % "1.5.8",
      "org.typelevel" %% "munit-cats-effect" % munitVersion % Test
    )
  )
