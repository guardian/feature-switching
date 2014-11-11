package com.gu.featureswitching

case class FeatureSwitch(key: String, title: String, default: Boolean)

trait FeatureSwitching extends FeatureSwitchingEnablingStrategy with FeatureSwitchingOverrideStrategy {
  val features: List[FeatureSwitch]

  def getFeatureFromKeyParam(featureKey: String, callback: () => Unit) = {
    features.find(_.key == featureKey) getOrElse callback() 
  }

  def featureIsActive(feature: FeatureSwitch): Boolean = {
    featureIsOverridden(feature) orElse featureIsEnabled(feature) getOrElse feature.default
  }
}

trait FeatureSwitchingEnablingStrategy {
  def featureIsEnabled(feature: FeatureSwitch): Option[Boolean]
  def featureSetEnabled(feature: FeatureSwitch, enabled: Boolean)
  def featureResetEnabled(feature: FeatureSwitch)
}

trait FeatureSwitchingOverrideStrategy {
  def featureIsOverridden(feature: FeatureSwitch): Option[Boolean]
  def featureSetOverride(feature: FeatureSwitch, overridden: Boolean)
  def featureResetOverride(feature: FeatureSwitch)
}
