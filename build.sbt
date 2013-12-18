name := "feature-switching"

version := "0.11-SNAPSHOT"

organization := "com.gu"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "net.liftweb" %% "lift-json" % "2.5",
  "org.slf4j" % "slf4j-api" % "1.6.1",
  "org.scalatra" %% "scalatra" % "2.0.5"
)

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
