package com.gu.featureswitching

trait FeaturesApi extends FeatureSwitching{
  case class FeatureSwitchEntity(key: String,
                                 title: String,
                                 default: Boolean,
                                 active: Boolean,
                                 enabled: ToggleResponse,
                                 overridden: ToggleResponse)
  
  case class ToggleResponse(uri: String, data: Option[Boolean])
  case class ErrorEntity(errorKey: String)
  case class StringEntity(data: String)
  case class FeatureSwitchRoot(switches: FeatureSwitchIndexResponse)
  case class FeatureSwitchRootResponse(data: FeatureSwitchRoot)
  case class FeatureSwitchIndexResponse(uri: Option[String], data: List[FeatureSwitchResponse])
  case class FeatureSwitchResponse(uri: Option[String], data: FeatureSwitchEntity)

  def baseApiUri: String
  def switchesUri = baseApiUri + "/switches"
  def switchUri(feature: FeatureSwitch) = switchesUri + "/" + feature.key
  def switchEnabledUri(feature: FeatureSwitch) = switchUri(feature) + "/enabled"
  def switchOverriddenUri(feature: FeatureSwitch) = switchUri(feature) + "/overridden"

  def featureResponse(feature: FeatureSwitch): FeatureSwitchResponse = {
    val entity = FeatureSwitchEntity(
      key        = feature.key,
      title      = feature.title,
      default    = feature.default,
      active     = featureIsActive(feature),
      enabled    = ToggleResponse(switchEnabledUri(feature),    featureIsEnabled(feature)),
      overridden = ToggleResponse(switchOverriddenUri(feature), featureIsOverridden(feature))
    )
    FeatureSwitchResponse(Some(switchUri(feature)), entity)
  }

  def featuresResponses: List[FeatureSwitchResponse] = features.map(featureResponse(_))
}
