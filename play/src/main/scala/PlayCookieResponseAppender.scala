package com.gu.featureswitching.play

import play.api.mvc.{Cookies, Cookie, Result}

trait PlayResultCookieAdder extends Cookies {
  val cookies: Option[Cookie]

  def appendCookies(result: Result) {
    cookies.foreach {result.withCookies(_)}
  }
}
