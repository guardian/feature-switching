package com.gu.featureswitching

import org.scalatra._

trait ScalatraCookieFeatureSwitchingOverrideStrategy extends
    CookieFeatureSwitchingOverrideStrategy with ScalatraBase {

  def getCookie(name: String) = cookies.get(name)
  def setCookie(name: String, value: String) = cookies.set(name, value)

}
