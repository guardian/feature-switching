import sbt._

object Dependencies {
  val liftJson = "net.liftweb" %% "lift-json" % "2.5"

  val sfl4j = "org.slf4j" % "slf4j-api" % "1.6.1"

  val scalatra = "org.scalatra" % "scalatra_2.9.1" % "2.0.2"

  val javaServlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"

  val amazonWebServicesSdk = "com.amazonaws" % "aws-java-sdk" % "1.6.3"
}
