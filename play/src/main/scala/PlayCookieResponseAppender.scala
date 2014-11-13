package com.gu.featureswitching.play

import play.api.mvc.{Cookies, Cookie, Result}

trait PlayResultCookieAdder extends Cookies {
  def appendCookies(result: Result, cookies: Option[Cookie]) {
    cookies.foreach {result.withCookies(_)}
  }
}
