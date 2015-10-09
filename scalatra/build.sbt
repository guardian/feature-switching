name := "feature-switching-scalatra"

publishArtifact := true

lazy val scalatraDependencies = Seq(
  "net.liftweb" %% "lift-json" % "2.6.2",
  "org.scalatra" %% "scalatra" % "2.3.0"
)

libraryDependencies ++= Common.dependencies

libraryDependencies ++= scalatraDependencies

scmInfo := Some(ScmInfo(url("https://github.com/guardian/feature-switching"),
  "scm:git:git@github.com:guardian/feature-switching.git"))

description := "Scalatra support for feature switches"

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

pomExtra := {
  <url>https://github.com/guardian/feature-switching</url>
    <developers>
      <developer>
        <id>theguardian</id>
        <name>The Guardian</name>
        <url>https://github.com/guardian</url>
      </developer>
    </developers>
}