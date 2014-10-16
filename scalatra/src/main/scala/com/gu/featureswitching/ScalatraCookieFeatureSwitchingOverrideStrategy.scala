package com.gu.featureswitching

import org.scalatra._

trait ScalatraCookieFeatureSwitchingOverrideStrategy extends
    CookieFeatureSwitchingOverrideStrategy with ScalatraKernel with CookieSupport {

  def getCookie(name: String) = cookies.get(name)
  def setCookies(name: String, value: String) = cookies.set(name, value)

}
