package com.gu.featureswitching

case class FeatureSwitch(key: String, title: String, default: Boolean)

case class FeatureState(
  val features: List[FeatureSwitch]
)

trait FeatureStrategy {
  val name: String
  def get(state: FeatureState, feature: FeatureSwitch): Option[Boolean]
  def set(state: FeatureState, feature: FeatureSwitch): FeatureState
  def reset(state: FeatureState, feature: FeatureSwitch): FeatureState
}

trait FeatureSwitching extends {
  val strategies: List[FeatureStrategy] = List()
  val features: List[FeatureSwitch]

  def getFeature(featureKey: String): Option[FeatureSwitch]  = {
    features.find(_.key == featureKey)
  }

  def getState: FeatureState = FeatureState(features)

  def featureIsActive(feature: FeatureSwitch): Boolean = {
    strategies.foldLeft(feature.default) { (memo, strategy) =>
      strategy.get(getState, feature) getOrElse memo
    }
    // featureIsOverridden(feature) orElse featureIsEnabled(feature) getOrElse feature.default
    // strategies.foldRight(feature.default)((strategy, enabled) => _.getOrElse(_))

    // strategies.map(_.get(getState, feature))
    //   .filterNot(_.isEmpty)
    //   .lastOption.getOrElse(feature.default)
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
