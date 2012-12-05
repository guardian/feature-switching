package com.gu.featureswitching

import collection.mutable.LinkedHashMap

// Simple in-memory implementation of feature state persistence.
// Obviously, don't use this if you have multiple app servers!
trait InMemoryFeatureSwitchEnablingStrategy extends FeatureSwitchingEnablingStrategy {
  var persistence: LinkedHashMap[FeatureSwitch, Boolean] = new LinkedHashMap[FeatureSwitch, Boolean]()

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
