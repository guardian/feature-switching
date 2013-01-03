package com.gu.featureswitching

import com.gu.featureswitching.util.Loggable
import org.scalatra._

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

  private def renderCookie(newCookieMap: Map[String, Boolean]) = newCookieMap.map {
    case (key, value) => "%s=%s".format(key, value)
  }.toList.mkString(",")

  private def resetCookie(newCookieMap: Map[String, Boolean]) {
    cookies.set(featureSwitchOverrideKey, renderCookie(newCookieMap))
  }


  /* Public interface */

  def featureIsOverridden(feature: FeatureSwitch): Option[Boolean] = {
    cookieMap.get(feature.key)
  }
  def featureSetOverride(feature: FeatureSwitch, overridden: Boolean) {
    val updatedCookieMap = cookieMap + (feature.key -> overridden)
    resetCookie(updatedCookieMap)
  }
  def featureResetOverride(feature: FeatureSwitch) {
    val updatedCookieMap = cookieMap - feature.key
    resetCookie(updatedCookieMap)
  }
}
