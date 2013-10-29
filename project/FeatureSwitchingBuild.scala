import sbt._
import Keys._
import Dependencies._
import SbtHelpers._

object ApplicationBuild extends Build {
  val version = "0.8"

  lazy val core = project("core", version)

  lazy val scalatraIntegration = project("scalatra-integration", version).dependsOn(core)
    .settings(libraryDependencies ++= Seq(scalatra, javaServlet, liftJson))

}
