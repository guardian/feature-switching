name := "feature-switching"

lazy val commonDependencies = Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.slf4j" % "slf4j-api" % "1.6.1"
)

lazy val scalatraDependencies = Seq(
  "net.liftweb" %% "lift-json" % "2.5",
  "org.scalatra" %% "scalatra" % "2.0.5"
)

lazy val commonSettings = Seq(
  version               := "0.13-SNAPSHOT",
  organization          := "com.gu",
  scalaVersion          := "2.10.0",
  libraryDependencies   ++= commonDependencies,
  scalacOptions         += "-deprecation"
)

lazy val core = Project("feature-switching-core", file("core"))
  .settings(commonSettings: _*)

lazy val scalatra = Project("feature-switching-scalatra", file("scalatra"))
  .settings((libraryDependencies ++= commonDependencies ++ scalatraDependencies) ++  commonSettings: _*)
  .dependsOn(core)

lazy val root = (project in file("."))
  .settings((publishArtifact := false) ++ commonSettings: _*)
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
