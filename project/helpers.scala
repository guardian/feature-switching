import sbt._
import Keys._
import Dependencies._

object SbtHelpers {
  val Organization = "com.gu"

  val commonSettings: Seq[Setting[_]] = Seq(
    resolvers += "Guardian GitHub Releases" at "http://guardian.github.com/maven/repo-releases"
  )

  def project(id: String, projectVersion: String) = Project(id = id, base = file(id))
    .settings(commonSettings: _*)
    .settings(
      name := id,
      version := projectVersion,
      organization := Organization,
      scalaVersion := "2.9.2",
      libraryDependencies ++= Seq(sfl4j, amazonWebServicesSdk),
      publishTo <<= (version) { version: String =>
        val publishType = if (version.endsWith("SNAPSHOT")) "snapshots" else "releases"
        Some(
          Resolver.file(
            "guardian github " + publishType,
            file(System.getProperty("user.home") + "/guardian.github.com/maven/repo-" + publishType)
          )
        )
      },
      scalacOptions += "-deprecation")
}
