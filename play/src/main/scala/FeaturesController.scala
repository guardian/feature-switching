package com.gu.featureswitching.play

import play.api.mvc._
import play.api.libs.json._
import com.gu.featureswitching.responses._
import com.gu.featureswitching.{FeatureSwitch, FeatureSwitching, FeaturesApi}


trait PlayFeaturesApi extends Controller with FeatureSwitching with FeaturesApi {
  val features: List[FeatureSwitch]

  implicit val stringEntitySerializer = Json.writes[StringEntity] 
  implicit val errorSerializer = Json.writes[ErrorEntity] 
  implicit val booleanEntitySerializer = Json.writes[BooleanEntity]

  implicit val featuresSerializer = Json.writes[FeatureSwitch] 
  implicit val toggleResponseSerializer = Json.writes[ToggleResponse] 
  implicit val featureSwitchEntitySerializer = Json.writes[FeatureSwitchEntity] 
  implicit val featureSwitchResponseSerializer = Json.writes[FeatureSwitchResponse] 

  implicit val featureSwitchIndexResponseSerializer = Json.writes[FeatureSwitchIndexResponse]
  implicit val featureSwitchRootSerializer = Json.writes[FeatureSwitchRoot] 
  implicit val featureSwitchRootResponseSerializer = Json.writes[FeatureSwitchRootResponse] 

  def getHealthCheck = Action { 
    Ok(Json.toJson(StringEntity("ok")))
  }

  def getRootResponse = Action {
    Ok(Json.toJson(FeatureSwitchRootResponse(FeatureSwitchRoot(
      FeatureSwitchIndexResponse(Some(switchesUri), featuresResponses)
    ))))    
  }

  def getFeatureList = Action {
    Ok(Json.toJson(FeatureSwitchIndexResponse(None, featuresResponses))) 
  }

  def getFeatureByKey(key: String) = Action {
    getFeature(key).fold(
      NotFound(Json.toJson(ErrorEntity("invalid-feature")))
    )(feature => {
      Ok(Json.toJson(featureResponse(feature))) 
    })
  }

  def getFeatureEnabledByKey(key: String) = Action {
    getFeature(key).fold(
      NotFound(Json.toJson(ErrorEntity("invalid-feature")))
    )(feature => {
      featureIsEnabled(feature).fold(
        NotFound(Json.toJson(ErrorEntity("unset-feature")))
      )(enabled => { 
        Ok(Json.toJson(BooleanEntity(enabled)))
      })
    }) 
  }

  def putFeatureEnabledByKey(key: String) = Action { request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json =>
      Ok(Json.toJson(BooleanEntity(true)))
    }.getOrElse {
      // log "Deserialisation failed (invalid json)"
      BadRequest(Json.toJson(ErrorEntity("invalid-json")))
    }
  }
}
