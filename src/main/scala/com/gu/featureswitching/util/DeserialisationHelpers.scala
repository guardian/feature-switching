package com.gu.featureswitching.util

import com.gu.featureswitching.dispatcher.ErrorEntity
import org.scalatra.ScalatraServlet
import net.liftweb.json.DefaultFormats
import net.liftweb.json.JsonAST.{JBool, JArray}
import net.liftweb.json.parse

trait DeserialisationHelpers extends ScalatraServlet with Loggable {
  lazy val errorInvalidJson = ErrorEntity("invalid-json")
  lazy val errorInvalidData = ErrorEntity("invalid-data")

  protected def parseBoolean(json: String): Boolean = {
    implicit val formats = DefaultFormats
    try {
      // circumvent strict JSON requirement of values being an array or an object
      parse("[%s]".format(json)) match {
        case JArray(JBool(s) :: Nil) => s
        case _ =>
          logger.info("Deserialisation failed (invalid data)")
          halt(status = 400, body = errorInvalidData)
      }
    }
    catch {
      case e: Exception =>
        logger.info("Deserialisation failed (invalid json)")
        halt(status = 400, body = errorInvalidJson)
    }
  }
}
