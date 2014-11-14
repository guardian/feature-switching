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

  val invalidFeature = NotFound(Json.toJson(ErrorEntity("invalid-feature")))
  val unsetFeature   = NotFound(Json.toJson(ErrorEntity("unset-feature")))
  val invalidData    = BadRequest(Json.toJson(ErrorEntity("invalid-data")))
  val invalidJson    = BadRequest(Json.toJson(ErrorEntity("invalid-json")))

  def orError[A](feature: Option[A], r: Result): Either[Result, A] = feature match { 
    case Some(f) => Right(f) 
    case None => Left(r)
  }

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
     (for { 
      feature <- orError(getFeature(key), invalidFeature).right
    } yield Ok(Json.toJson(featureResponse(feature)))).merge
  }

  def getFeatureOverriddenByKey(key: String) = Action {
    (for { 
      feature <- orError(getFeature(key), invalidFeature).right
      enabled <- orError(featureIsOverridden(feature), unsetFeature).right 
    } yield Ok(Json.toJson(BooleanEntity(enabled)))).merge
  }

  def getFeatureEnabledByKey(key: String) = Action {
    (for { 
      feature <- orError(getFeature(key), invalidFeature).right
      enabled <- orError(featureIsEnabled(feature), unsetFeature).right 
    } yield Ok(Json.toJson(BooleanEntity(enabled)))).merge
  }

  def deleteFeatureEnabledByKey(key: String) = Action { request =>
    (for { 
      feature <- orError(getFeature(key), invalidFeature).right
    } yield { 
      featureResetEnabled(feature)
      Ok
    }).merge
  }

  def putFeatureEnabledByKey(key: String) = Action { request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    (for {
      json    <- orError(jsonBody, invalidJson).right
      feature <- orError(getFeature(key), invalidFeature).right 
      enabled <- orError(json.validate[BooleanEntity].asOpt, invalidData).right
    } yield {
      featureSetEnabled(feature, enabled.data)
      Ok
    }).merge
  }
}
