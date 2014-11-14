package com.gu.featureswitching.play

import play.api.mvc._
import play.api.libs.json._
import com.gu.featureswitching.responses._
import com.gu.featureswitching.{FeatureSwitch, FeatureSwitching, FeaturesApi}


trait PlayFeaturesApi extends Controller with FeatureSwitching with FeaturesApi {
  val features: List[FeatureSwitch]

  implicit val stringEntityWrite = Json.writes[StringEntity] 
  implicit val errorWrite = Json.writes[ErrorEntity] 
  implicit val booleanEntityWrite = Json.writes[BooleanEntity]
  implicit val booleanEntityRead = Json.reads[BooleanEntity]

  implicit val featuresWrite = Json.writes[FeatureSwitch] 
  implicit val toggleResponseWrite = Json.writes[ToggleResponse] 
  implicit val featureSwitchEntityWrite = Json.writes[FeatureSwitchEntity] 
  implicit val featureSwitchResponseWrite = Json.writes[FeatureSwitchResponse] 

  implicit val featureSwitchIndexResponseWrite = Json.writes[FeatureSwitchIndexResponse]
  implicit val featureSwitchRootWrite = Json.writes[FeatureSwitchRoot] 
  implicit val featureSwitchRootResponseWrite = Json.writes[FeatureSwitchRootResponse] 

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
      getFeature(key).fold(
        NotFound(Json.toJson(ErrorEntity("invalid-feature")))
      )(feature => {
        val jsResult = json.validate[BooleanEntity] 
        jsResult match {
          case JsSuccess(booleanEntity, path) => {
            featureSetEnabled(feature, booleanEntity.data)
            Ok
          }
          case JsError(error) => BadRequest(Json.toJson(ErrorEntity("invalid-data")))
        }  
      })
    }.getOrElse {
      BadRequest(Json.toJson(ErrorEntity("invalid-json")))
    }
  }
}
