ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.1"

lazy val root = (project in file("."))
  .settings(
    name := "zio-http-demo",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.5",
      "dev.zio" %% "zio-streams" % "2.0.5",
      "dev.zio" %% "zio-json" % "0.4.2",
      "io.d11" %% "zhttp" % "2.0.0-RC11"
    )
  )
