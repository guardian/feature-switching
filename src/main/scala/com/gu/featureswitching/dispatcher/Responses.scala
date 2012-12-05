package com.gu.featureswitching.dispatcher

case class FeatureSwitchEntity(key: String,
                               title: String,
                               default: Boolean,
                               active: Boolean,
                               enabled: Option[Boolean],
                               overridden: Option[Boolean])

case class LinkEntity(rel: String, href: String)
case class ErrorEntity(errorKey: String)

case class FeatureSwitchSummaryResponse(data: Map[String, Boolean], links: List[LinkEntity] = List())
case class FeatureSwitchIndexResponse(data: List[FeatureSwitchResponse], links: List[LinkEntity] = List())
case class FeatureSwitchResponse(uri: Option[String], data: FeatureSwitchEntity, links: List[LinkEntity] = List())
