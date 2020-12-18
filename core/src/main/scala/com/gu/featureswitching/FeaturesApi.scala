package com.gu.featureswitching

import com.gu.featureswitching.responses._

trait FeaturesApi {
  def baseApiUri: String
  def switchesUri = baseApiUri + "/switches"
  def switchUri(feature: FeatureSwitch) = switchesUri + "/" + feature.key
  def switchEnabledUri(feature: FeatureSwitch) = switchUri(feature) + "/enabled"
  def switchOverriddenUri(feature: FeatureSwitch) = switchUri(feature) + "/overridden"

  def featureResponse(feature: FeatureSwitch, isActive: Boolean): FeatureSwitchResponse = {
    val entity = FeatureSwitchEntity(
      key        = feature.key,
      title      = feature.title,
      default    = feature.default,
      active     = isActive
    )
    FeatureSwitchResponse(Some(switchUri(feature)), entity)
  }

  def featuresResponses(featureSwitching: FeatureSwitching): List[FeatureSwitchResponse] =
    featureSwitching.features.map { (feature) =>
      featureResponse(feature, featureSwitching.featureIsActive(feature))
    }
}
