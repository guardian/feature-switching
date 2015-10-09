import ReleaseTransformations._

name in ThisBuild := "feature-switching"

organization in ThisBuild := "com.gu"

scalaVersion in ThisBuild := "2.11.7"

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

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)
