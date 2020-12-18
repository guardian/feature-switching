package com.gu.featureswitching.responses

case class FeatureSwitchEntity(key: String,
                               title: String,
                               default: Boolean,
                               active: Boolean)

case class ErrorEntity(errorKey: String)
case class StringEntity(data: String)
case class BooleanEntity(data: Boolean)
case class FeatureSwitchRoot(switches: FeatureSwitchIndexResponse)
case class FeatureSwitchRootResponse(data: FeatureSwitchRoot)
case class FeatureSwitchIndexResponse(uri: Option[String], data: List[FeatureSwitchResponse])
case class FeatureSwitchResponse(uri: Option[String], data: FeatureSwitchEntity)
