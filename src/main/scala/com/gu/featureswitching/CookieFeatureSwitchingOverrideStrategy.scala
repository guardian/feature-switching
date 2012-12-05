package com.gu.featureswitching

import scala.collection.mutable.LinkedHashMap
import com.gu.featureswitching.util.Loggable
import org.scalatra._


// TODO: generic cookie support?

trait CookieFeatureSwitchingOverrideStrategy extends FeatureSwitchingOverrideStrategy
    with ScalatraKernel with CookieSupport with Loggable {


  lazy val featureSwitchOverrideKey = "features.override"

  // TODO: clear obsolete keys for non-existing features, by reading the list of existing switches
  private def rawValue = cookies.get(featureSwitchOverrideKey).getOrElse("")

  private def cookieMap = rawValue.split(",").map(_.split("=").toList).flatMap {
    case List(key, "true") => Some((key -> true))
    case List(key, "false") => Some((key -> false))
    case _ => None // invalid token, ignore
  }.toMap

  private var setterMap = new LinkedHashMap[String, Option[Boolean]]()
  private def valueMap = setterMap.foldLeft(cookieMap) {
    case (values, (key, Some(value))) => values + (key -> value)
    case (values, (key, None)) => values - key
  }

  private def renderCookie = valueMap.map {
    case (key, value) => "%s=%s".format(key, value)
  }.toList.mkString(",")


  after() {
    val updatedValue = renderCookie
    if (updatedValue != rawValue) {
      logger.info("Updating %s cookie: %s".format(featureSwitchOverrideKey, updatedValue))
      // TODO: is this actually working?
      cookies.set(featureSwitchOverrideKey, updatedValue)
    }
  }


  def featureIsOverridden(feature: FeatureSwitch): Option[Boolean] = {
    valueMap.get(feature.key)
  }
  def featureSetOverride(feature: FeatureSwitch, overridden: Boolean) {
    setterMap += (feature.key -> Some(overridden))
  }
  def featureResetOverride(feature: FeatureSwitch) {
    setterMap += (feature.key -> None)
  }
}
