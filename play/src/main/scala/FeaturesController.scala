package com.gu.featureswitching.play

import play.api.mvc._
import play.api.libs.json._
import com.gu.featureswitching.{FeatureSwitch, FeatureSwitching}

trait FeaturesApi extends Controller with FeatureSwitching{
  val features: List[FeatureSwitch]
  implicit val featuresSerializer = Json.writes[FeatureSwitch] 

  def healthCheck = Action { 
    Ok("ok")
  }

  def featureList = Action {
    implicit val featuresSerializer = Json.writes[FeatureSwitch] 

    Ok(Json.toJson(features))
  }

  def featureByKey(key: String) = Action {
    implicit val featuresSerializer = Json.writes[FeatureSwitch] 

    getFeature(key).fold(NotFound("invalid-feature"))(f => Ok(Json.toJson(f)))
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
