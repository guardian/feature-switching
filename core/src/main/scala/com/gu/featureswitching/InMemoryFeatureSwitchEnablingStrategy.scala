package com.gu.featureswitching

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._

// Simple in-memory implementation of feature state persistence.
// Obviously, don't use this if you have multiple app servers!
trait InMemoryFeatureSwitchEnablingStrategy extends FeatureSwitchingEnablingStrategy {
  val persistence = new ConcurrentHashMap[FeatureSwitch, Boolean]().asScala

  def featureIsEnabled(feature: FeatureSwitch): Option[Boolean] = {
    persistence.get(feature)
  }

  def featureSetEnabled(feature: FeatureSwitch, enabled: Boolean) {
    persistence(feature) = enabled
  }

  def featureResetEnabled(feature: FeatureSwitch) {
    persistence.remove(feature)
  }
}
