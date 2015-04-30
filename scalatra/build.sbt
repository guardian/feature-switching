name := "feature-switching-scalatra"

publishArtifact := true

lazy val scalatraDependencies = Seq(
  "net.liftweb" %% "lift-json" % "2.6.2",
  "org.scalatra" %% "scalatra" % "2.3.0"
)

libraryDependencies ++= Common.dependencies

libraryDependencies ++= scalatraDependencies
