ThisBuild / version := "0.1.4"

ThisBuild / scalaVersion := "2.12.13"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.10" % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "pretty-sql-sbt-plugin",
    sbtPlugin := true
  )
