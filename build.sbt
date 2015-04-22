import sbtrelease._
import ReleaseStateTransformations._

releaseSettings

sonatypeSettings

name in ThisBuild := "feature-switching"

version in ThisBuild := "1.0-SNAPSHOT"

organization in ThisBuild := "com.gu"

scalaVersion in ThisBuild := "2.11.6"

libraryDependencies ++= Common.dependencies

scalacOptions in ThisBuild += "-deprecation"

publishArtifact := false

lazy val core = Project("core", file("core"))

lazy val scalatra = Project("scalatra", file("scalatra"))
  .dependsOn(core)

lazy val root = Project("root", file("."))
  .aggregate(core, scalatra)

scmInfo := Some(ScmInfo(url("https://github.com/guardian/feature-switching"),
  "scm:git:git@github.com:guardian/feature-switching.git"))

description := "A library for creating and managing feature switches"

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

ReleaseKeys.releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(
    action = state => Project.extract(state).runTask(PgpKeys.publishSigned, state)._1,
    enableCrossBuild = true
  ),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(state => Project.extract(state).runTask(SonatypeKeys.sonatypeReleaseAll, state)._1),
  pushChanges
)
