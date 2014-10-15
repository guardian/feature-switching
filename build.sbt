name                := "feature-switching"

version             := "0.13-SNAPSHOT"

organization        := "com.gu"

scalaVersion        := "2.10.0"

libraryDependencies ++= Common.dependencies

scalacOptions       += "-deprecation"

publishArtifact     := false

lazy val core = Project("feature-switching-core", file("core"))

lazy val scalatra = Project("feature-switching-scalatra", file("scalatra"))
  .dependsOn(core)

publishTo <<= (version) { version: String =>
    val publishType = if (version.endsWith("SNAPSHOT")) "snapshots" else "releases"
    Some(
        Resolver.file(
            "guardian github " + publishType,
            file(System.getProperty("user.home") + "/guardian.github.com/maven/repo-" + publishType)
        )
    )
}
