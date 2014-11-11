package com.gu.featureswitching.play

import play.api.mvc._
import play.api.libs.json._

import com.gu.featureswitching.FeatureSwitch

trait FeaturesApi extends Controller {
  val features: List[FeatureSwitch]

  def healthCheck = Action {
    Ok("ok")
  }

  def featureList = Action {
    implicit val featuresSerializer = Json.writes[FeatureSwitch] 

    Ok(Json.toJson(features))
  }

}
