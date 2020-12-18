name := "feature-switching-play"

lazy val playDependencies = Seq(
  "com.typesafe.play" %% "play" % "2.3.2" 
)

lazy val testDependencies = Seq(
  "com.typesafe.play" %% "play-test" % "2.3.2" % "test", 
  "org.specs2" %% "specs2-core" % "2.4.11" % "test"
)

libraryDependencies ++= Common.dependencies

libraryDependencies ++= playDependencies

libraryDependencies ++= testDependencies
