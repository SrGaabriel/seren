ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.0"

lazy val frontend = (project in file("frontend"))
  .settings(
    name := "seren-frontend",
    idePackagePrefix := Some("me.gabriel.seren.frontend")
  )

lazy val compiler = (project in file("compiler"))
  .settings(
    name := "seren-compiler",
    idePackagePrefix := Some("me.gabriel.seren.compiler")
  )
  .dependsOn(frontend)