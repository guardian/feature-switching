name in ThisBuild := "feature-switching"

version in ThisBuild := "0.13-SNAPSHOT"

organization in ThisBuild := "com.gu"

scalaVersion in ThisBuild := "2.10.0"

libraryDependencies ++= Common.dependencies

scalacOptions in ThisBuild += "-deprecation"

publishArtifact := false

lazy val core = Project("core", file("core"))

lazy val scalatra = Project("scalatra", file("scalatra"))
  .dependsOn(core)

lazy val root = Project("root", file("."))
  .aggregate(core, scalatra)

publishTo <<= (version) { version: String =>
    val publishType = if (version.endsWith("SNAPSHOT")) "snapshots" else "releases"
    Some(
        Resolver.file(
            "guardian github " + publishType,
            file(System.getProperty("user.home") + "/guardian.github.com/maven/repo-" + publishType)
        )
    )
}
