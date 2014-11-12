package com.gu.featureswitching.play

import com.gu.featureswitching.CookieFeatureSwitchingOverrideStrategy
import play.api.mvc.{Cookies, Cookie}

trait PlayCookieFeatureSwitchingOverrideStrategy extends CookieFeatureSwitchingOverrideStrategy with Cookies with PlayResultCookieAdder { 
  def getCookie(name: String) = get(name) map {_.value}
  def setCookie(name: String, value: String) = {
    val featureSwitchingOverrideStrategyCookie = Cookie(name, value)
  }
}
