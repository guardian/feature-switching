package com.gu.featureswitching

case class DefaultFeatureState(features: List[FeatureSwitch]) extends FeatureState
case class FeatureSwitch(key: String, title: String, default: Boolean)

trait FeatureState {
  val features: List[FeatureSwitch]

  def getFeature(featureKey: String): Option[FeatureSwitch]  = {
    features.find(_.key == featureKey)
  }
}

trait FeatureStrategy[StateType] {
  val name: String
  def get(state: StateType, feature: FeatureSwitch): Option[Boolean]
  def set(state: StateType, feature: FeatureSwitch): StateType
  def reset(state: StateType, feature: FeatureSwitch): StateType
}

trait FeatureSwitching {
  type StateType

  val strategies: List[FeatureStrategy[StateType]]

  def featureIsActive(feature: FeatureSwitch, state: StateType): Boolean = {
    strategies.foldLeft(feature.default) { (memo, strategy) =>
      strategy.get(state, feature) getOrElse memo
    }
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
