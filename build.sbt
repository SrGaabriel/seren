ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.0"

lazy val frontend = (project in file("frontend"))
  .settings(
    name := "seren-frontend",
    idePackagePrefix := Some("me.gabriel.seren.frontend")
  )

lazy val analyzer = (project in file("analyzer"))
  .settings(
    name := "seren-analyzer",
    idePackagePrefix := Some("me.gabriel.seren.analyzer")
  )
  .dependsOn(frontend)

lazy val compiler = (project in file("compiler"))
  .settings(
    name := "seren-compiler",
    idePackagePrefix := Some("me.gabriel.seren.compiler")
  )
  .dependsOn(analyzer)

lazy val llvm = (project in file("llvm"))
  .settings(
    name := "seren-llvm",
    idePackagePrefix := Some("me.gabriel.seren.llvm")
  )

lazy val tianlong = (project in file("tianlong"))
  .aggregate(frontend, analyzer, compiler)
  .settings(
    name := "tianlong",
    idePackagePrefix := Some("me.gabriel.tianlong"),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test
  )

