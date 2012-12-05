package com.gu.featureswitching.util

import org.slf4j.LoggerFactory

trait Loggable {
  protected lazy val logger = LoggerFactory.getLogger(getClass)
}
