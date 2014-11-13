package com.gu.featureswitching.play

import play.api.mvc._
import play.api.libs.json._
import com.gu.featureswitching.{FeatureSwitch, FeatureSwitching, FeaturesApi}


trait PlayFeaturesApi extends Controller with FeatureSwitching with FeaturesApi {
  val features: List[FeatureSwitch]
  implicit val featuresSerializer = Json.writes[FeatureSwitch] 

  def healthCheck = Action { 
    implicit val stringEntitySerializer = Json.writes[StringEntity] 

    Ok(
      Json.toJson(
        StringEntity("ok")  
      )
    )
  }

  def featureList = Action {
    implicit val featuresSerializer = Json.writes[FeatureSwitch] 

    Ok(Json.toJson(features))
  }

  def featureByKey(key: String) = Action {
    implicit val errorSerializer = Json.writes[ErrorEntity] 
    implicit val featuresSerializer = Json.writes[FeatureSwitch] 
    implicit val featuresSwitchEntitySerializer = Json.writes[FeatureSwitchEntity] 
    implicit val featuresSwitchResponseSerializer = Json.writes[FeatureSwitchResponse] 
    implicit val toggleResponseSerializer = Json.writes[ToggleResponse] 

    getFeature(key).fold(
      NotFound(
        Json.toJson(
          ErrorEntity(
            "invalid-feature"
          )
        )
      )
    )(f => {
      Ok(
        Json.toJson(
          featureResponse(f) 
        )
      )
    })
  }

  def featureEnabledByKey(key: String) = Action {
    implicit val featuresSerializer = Json.writes[FeatureSwitch] 

    getFeature(key).fold(NotFound("invalid-feature"))(f => {
      featureIsEnabled(f).fold(NotFound("unset-feature"))(e => Ok(Json.toJson(e)))
    }) 
  }

  def featureOverridenByKey(key: String) = Action {
    Ok("ok")
  }
}
