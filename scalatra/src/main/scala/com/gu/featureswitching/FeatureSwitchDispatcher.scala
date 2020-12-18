package com.gu.featureswitching.dispatcher

import org.scalatra.ScalatraServlet
import com.gu.featureswitching.responses._
import com.gu.featureswitching.util.DeserialisationHelpers
import com.gu.featureswitching.{FeatureSwitch, FeatureSwitching}
import net.liftweb.json.DefaultFormats


trait FeatureSwitchDispatcher extends ScalatraServlet
    with JsonDispatcher with DeserialisationHelpers with FeatureSwitching {

  implicit val defaultFormats = DefaultFormats

  lazy val errorInvalidFeature = ErrorEntity("invalid-feature")
  lazy val errorFeatureNotSet = ErrorEntity("unset-feature")

  // Classes extending this trait need to provide the absolute URI to this dispatcher
  def baseApiUri: String

  // Define URIs
  def switchesUri = baseApiUri + "/switches"
  def switchUri(feature: FeatureSwitch) = switchesUri + "/" + feature.key
  def switchEnabledUri(feature: FeatureSwitch) = switchUri(feature) + "/enabled"
  def switchOverriddenUri(feature: FeatureSwitch) = switchUri(feature) + "/overridden"


  // disable caching
  after() {
    response.setHeader("Cache-Control", "public, max-age=0")
  }

  def noContent = {
    status_=(201)
  }

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


  get("/") {
    val index = FeatureSwitchIndexResponse(Some(switchesUri), featuresResponses)
    FeatureSwitchRootResponse(FeatureSwitchRoot(index))
  }

  get("/switches") {
    FeatureSwitchIndexResponse(None, featuresResponses)
  }

  // You probably only want to use this for the routes below
  def getFeatureOr404 = {
    getFeature(params("key")) getOrElse halt(404, body = errorInvalidFeature)
  }

  get("/switches/:key") {
    val feature = getFeatureOr404

    featureResponse(feature)
  }

  get("/switches/:key/enabled") {
    val feature = getFeatureOr404

    featureIsEnabled(feature) getOrElse halt(404, errorFeatureNotSet)
  }

  put("/switches/:key/enabled") {
    val feature = getFeatureOr404 
    val value = parseBoolean(request.body)

    featureSetEnabled(feature, value)
    noContent
  }

  delete("/switches/:key/enabled") {
    val feature = getFeatureOr404 

    featureResetEnabled(feature)
    noContent
  }

  get("/switches/:key/overridden") {
    val feature = getFeatureOr404 

    featureIsOverridden(feature) getOrElse halt(404, errorFeatureNotSet)
  }

  put("/switches/:key/overridden") {
    val feature = getFeatureOr404 
    val value = parseBoolean(request.body)
    featureSetOverride(feature, value)
    noContent
  }

  delete("/switches/:key/overridden") {
    val feature = getFeatureOr404 
    featureResetOverride(feature)
    noContent
  }
}
