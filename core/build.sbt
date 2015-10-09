name := "feature-switching-core"

publishArtifact := true

scmInfo := Some(ScmInfo(url("https://github.com/guardian/feature-switching"),
  "scm:git:git@github.com:guardian/feature-switching.git"))

description := "A library for creating and managing feature switches"

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