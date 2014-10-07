name := "feature-switching"

version in ThisBuild := "0.13-SNAPSHOT"

organization := "com.gu"

scalaVersion := "2.10.0"

lazy val commonDependencies = Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.slf4j" % "slf4j-api" % "1.6.1"
)

lazy val scalatraDependencies = Seq(
  "net.liftweb" %% "lift-json" % "2.5",
  "org.scalatra" %% "scalatra" % "2.0.5"
)

lazy val core = Project("feature-switching-core", file("core"))
  .settings(libraryDependencies ++= commonDependencies)

lazy val scalatra = Project("feature-switching-scalatra", file("scalatra"))
  .settings(libraryDependencies ++= commonDependencies ++ scalatraDependencies)
  .dependsOn(core)

lazy val root = (project in file("."))
  .settings(publishArtifact := false)
  .dependsOn(core, scalatra)

publishTo <<= (version) { version: String =>
    val publishType = if (version.endsWith("SNAPSHOT")) "snapshots" else "releases"
    Some(
        Resolver.file(
            "guardian github " + publishType,
            file(System.getProperty("user.home") + "/guardian.github.com/maven/repo-" + publishType)
        )
    )
}


scalacOptions += "-deprecation"
