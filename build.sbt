ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "2.12.13"

lazy val root = (project in file("."))
  .settings(
    name := "pretty-sql-sbt-plugin",
    sbtPlugin := true
  )
