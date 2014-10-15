package com.gu.featureswitching

import org.scalatra._
import java.net.{URLDecoder, URLEncoder}

trait ScalatraCookieFeatureSwitchingOverrideStrategy extends
    CookieFeatureSwitchingOverrideStrategy with ScalatraKernel with CookieSupport {

  def getCookie(name: String) = cookies.get(name)
  def setCookies(name: String, value: String) = cookies.set(name, value)

}
