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

  def healthCheck = Action { 
    Ok(Json.toJson(StringEntity("ok")))
  }

  def rootResponse = Action {
    Ok(Json.toJson(FeatureSwitchRootResponse(FeatureSwitchRoot(
      FeatureSwitchIndexResponse(Some(switchesUri), featuresResponses)
    ))))    
  }

  def featureList = Action {
    Ok(Json.toJson(FeatureSwitchIndexResponse(None, featuresResponses))) 
  }

  def featureByKey(key: String) = Action {
    getFeature(key).fold(
      NotFound(Json.toJson(ErrorEntity("invalid-feature")))
    )(f => {
      Ok(Json.toJson(featureResponse(f))) 
    })
  }

  def featureEnabledByKey(key: String) = Action {
    getFeature(key).fold(
      NotFound(Json.toJson(ErrorEntity("invalid-feature")))
    )(f => {
      featureIsEnabled(f).fold(
        NotFound(Json.toJson(ErrorEntity("unset-feature")))
      )(e => { 
        Ok(Json.toJson(BooleanEntity(e)))
      })
    }) 
  }
}
