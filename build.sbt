ThisBuild / scalaVersion := "3.9.0"

lazy val root = (project in file("."))
    .settings(
        name := "ws-chat",
        organization := "otus",
        version := "0.1"
    )
    