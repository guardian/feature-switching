package com.gu.featureswitching

import com.gu.featureswitching.responses._

trait FeaturesApi extends FeatureSwitching {
  def baseApiUri: String
  def switchesUri = baseApiUri + "/switches"
  def switchUri(feature: FeatureSwitch) = switchesUri + "/" + feature.key
  def switchEnabledUri(feature: FeatureSwitch) = switchUri(feature) + "/enabled"
  def switchOverriddenUri(feature: FeatureSwitch) = switchUri(feature) + "/overridden"

  def featureResponse(feature: FeatureSwitch, state: FeatureState): FeatureSwitchResponse = {
    val entity = FeatureSwitchEntity(
      key        = feature.key,
      title      = feature.title,
      default    = feature.default,
      active     = featureIsActive(feature, state)
    )
    FeatureSwitchResponse(Some(switchUri(feature)), entity)
  }

  def featuresResponses: List[FeatureSwitchResponse] = features.map(featureResponse(_))
}
