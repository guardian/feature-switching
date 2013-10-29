package com.gu.featureswitching.scalatra.dispatcher

import org.scalatra.ScalatraServlet
import com.gu.featureswitching.util.DeserialisationHelpers
import com.gu.featureswitching.FeatureSwitching
import net.liftweb.json.DefaultFormats

trait FeatureSwitchDispatcher extends ScalatraServlet
    with JsonDispatcher with DeserialisationHelpers with FeatureSwitching {

  implicit val defaultFormats = DefaultFormats

  lazy val errorInvalidFeature = ErrorEntity("invalid-feature")
  lazy val errorFeatureNotSet = ErrorEntity("unset-feature")

  // Classes extending this trait need to provide the absolute URI to this dispatcher
  def baseApiUri: String


  // disable caching
  after() {
    response.setHeader("Cache-Control", "public, max-age=0")
  }

  def noContent = {
    status(201)
  }


  get("/") {
    val serverStates = features.map(feat => (feat.key -> featureIsActive(feat))).toMap

    val switchesLink = LinkEntity("switches", baseApiUri + "/switches")
    FeatureSwitchSummaryResponse(serverStates, List(switchesLink))
  }

  get("/switches") {
    val featureResponses = features.map {
      feature =>
        val entity = FeatureSwitchEntity(
          key = feature.key,
          title = feature.title,
          default = feature.default,
          active = featureIsActive(feature),
          enabled = featureIsEnabled(feature),
          overridden = featureIsOverridden(feature)
        )
        val entityUri = baseApiUri + "/switches/" + feature.key
        FeatureSwitchResponse(Some(entityUri), entity)
    }

    FeatureSwitchIndexResponse(featureResponses, List(
      LinkEntity("item:enabled",    baseApiUri + "/switches/{key}/enabled"),
      LinkEntity("item:overridden", baseApiUri + "/switches/{key}/overridden")
    ))
  }


  // You probably only want to use this for the routes below
  def getFeatureFromKeyParam = {
    val featureKey = params("key")
    features.find(_.key == featureKey) getOrElse halt(404, body = errorInvalidFeature)
  }

  get("/switches/:key") {
    val feature = getFeatureFromKeyParam

    val entity = FeatureSwitchEntity(
      key = feature.key,
      title = feature.title,
      default = feature.default,
      active = featureIsActive(feature),
      enabled = featureIsEnabled(feature),
      overridden = featureIsOverridden(feature)
    )

    FeatureSwitchResponse(None, entity,
      LinkEntity("enabled",    baseApiUri + "/switches/" + feature.key + "/enabled") ::
      LinkEntity("overridden", baseApiUri + "/switches/" + feature.key + "/overridden") ::
      Nil)
  }

  get("/switches/:key/enabled") {
    val feature = getFeatureFromKeyParam
    featureIsEnabled(feature) getOrElse halt(404, errorFeatureNotSet)
  }

  put("/switches/:key/enabled") {
    val feature = getFeatureFromKeyParam
    val value = parseBoolean(request.body)
    featureSetEnabled(feature, value)
    noContent
  }

  delete("/switches/:key/enabled") {
    val feature = getFeatureFromKeyParam
    featureResetEnabled(feature)
    noContent
  }

  get("/switches/:key/overridden") {
    val feature = getFeatureFromKeyParam
    featureIsOverridden(feature) getOrElse halt(404, errorFeatureNotSet)
  }

  put("/switches/:key/overridden") {
    val feature = getFeatureFromKeyParam
    val value = parseBoolean(request.body)
    featureSetOverride(feature, value)
    noContent
  }

  delete("/switches/:key/overridden") {
    val feature = getFeatureFromKeyParam
    featureResetOverride(feature)
    noContent
  }
}
