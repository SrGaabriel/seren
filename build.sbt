import java.io.File

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.0"

lazy val logging = (project in file("logging"))
  .settings(
    name := "seren-logging",
    idePackagePrefix := Some("me.gabriel.seren.logging")
  )

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
  .dependsOn(frontend, logging)

lazy val tianlong = (project in file("tianlong"))
  .aggregate(frontend, analyzer)
  .settings(
    name := "tianlong",
    idePackagePrefix := Some("me.gabriel.tianlong"),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test
  )

lazy val llvm = (project in file("llvm"))
  .settings(
    name := "seren-llvm",
    idePackagePrefix := Some("me.gabriel.seren.llvm")
  )
  .dependsOn(analyzer)
  .dependsOn(tianlong)

lazy val compiler = (project in file("compiler"))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "seren-compiler",
    idePackagePrefix := Some("me.gabriel.seren.compiler"),
    Compile / mainClass := Some("me.gabriel.seren.compiler.main"),
    javacOptions ++= Seq(
      "--release", "--target 21",
      "-Xlint:all"
    ),
    nativeImageVersion := "21.0.0",
    nativeImageJvm := "graalvm",
    nativeImageOptions ++= Seq(
      "--no-fallback"
    ),
    assembly / assemblyJarName := "seren-compiler.jar",
  )
  .dependsOn(llvm)