lazy val scalatraDependencies = Seq(
  "net.liftweb" %% "lift-json" % "2.5",
  "org.scalatra" %% "scalatra" % "2.0.5"
)

libraryDependencies ++= Common.dependencies

libraryDependencies ++= scalatraDependencies
