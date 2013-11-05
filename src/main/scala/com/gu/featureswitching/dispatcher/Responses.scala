package com.gu.featureswitching.dispatcher

// TODO: use generic entity/response models

case class FeatureSwitchEntity(key: String,
                               title: String,
                               default: Boolean,
                               active: Boolean,
                               enabled: ToggleResponse,
                               overridden: ToggleResponse)

case class ToggleResponse(uri: String, data: Option[Boolean])

// No links at this stage
//case class LinkEntity(rel: String, href: String)
case class ErrorEntity(errorKey: String)

case class FeatureSwitchRoot(switches: FeatureSwitchIndexResponse)

case class FeatureSwitchRootResponse(data: FeatureSwitchRoot)
case class FeatureSwitchIndexResponse(uri: Option[String], data: List[FeatureSwitchResponse])
case class FeatureSwitchResponse(uri: Option[String], data: FeatureSwitchEntity)
