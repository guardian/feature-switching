import sbt._
import Keys._

object Common {
  val dependencies = Seq(
    "javax.servlet" % "servlet-api" % "2.5" % "provided",
    "org.slf4j" % "slf4j-api" % "1.6.1"
  )
}
