package com.gu.featureswitching.play

import play.api.mvc._
import play.api.libs.json._
import com.gu.featureswitching.responses._
import com.gu.featureswitching.{play => _, _}

trait PlayRequestModifier {
  def apply(res: Result) = res
}

trait PlayHandler extends FeatureSwitching with PlayRequestModifier {
  val features: List[FeatureSwitch]
  val request: Request[AnyContent]
  val inCookies  = request.cookies
}

trait PlayCookieOverrideStrategy extends CookieFeatureSwitchingOverrideStrategy with PlayRequestModifier {
  val inCookies: Cookies
  val outCookies = scala.collection.mutable.ListBuffer[Cookie]()

  override def apply(res: Result): Result = super.apply(res.withCookies(outCookies.toList: _*))
  def getCookie(name: String): Option[String] =
    inCookies.get(name).map(_.name)
  def setCookie(name: String, value: String): Unit =
    outCookies += Cookie(name, value)
}

trait PlayFeaturesApi extends Controller with FeaturesApi {
  val features: List[FeatureSwitch]

  def getHandler(req: Request[AnyContent]): PlayHandler =
    new DefaultPlayHandler(features, req)

  def PlayFeatureAction(f: (Request[AnyContent], FeatureSwitching) => Result) =
    Action { request =>
      val handler = getHandler(request)
      handler(f(request, handler))
    }

  implicit val stringEntityWrite = Json.writes[StringEntity] 
  implicit val errorWrite = Json.writes[ErrorEntity] 
  implicit val booleanEntityWrite = Json.writes[BooleanEntity]
  implicit val booleanEntityRead = Json.reads[BooleanEntity]

  implicit val featuresWrite = Json.writes[FeatureSwitch] 
  implicit val featureSwitchEntityWrite = Json.writes[FeatureSwitchEntity] 
  implicit val featureSwitchResponseWrite = Json.writes[FeatureSwitchResponse] 

  implicit val featureSwitchIndexResponseWrite = Json.writes[FeatureSwitchIndexResponse]
  implicit val featureSwitchRootWrite = Json.writes[FeatureSwitchRoot] 
  implicit val featureSwitchRootResponseWrite = Json.writes[FeatureSwitchRootResponse] 

  val invalidFeature = NotFound(Json.toJson(ErrorEntity("invalid-feature")))
  val unsetFeature   = NotFound(Json.toJson(ErrorEntity("unset-feature")))
  val invalidData    = BadRequest(Json.toJson(ErrorEntity("invalid-data")))
  val invalidJson    = BadRequest(Json.toJson(ErrorEntity("invalid-json")))

  def getHealthCheck = Action { 
    Ok(Json.toJson(StringEntity("ok")))
  }

  def getRootResponse = PlayFeatureAction { (request, featureSwitching) =>
    Ok(Json.toJson(FeatureSwitchRootResponse(
                     FeatureSwitchRoot(
                       FeatureSwitchIndexResponse(Some(switchesUri),
                                                  featuresResponses(featureSwitching)
    )))))
  }

  def getFeatureList = PlayFeatureAction { (request, featureSwitching) =>
    Ok(Json.toJson(FeatureSwitchIndexResponse(None, featuresResponses(featureSwitching))))
  }

  def getFeatureByKey(key: String) = PlayFeatureAction { (request, featureSwitching) =>
     (for { 
      feature <- featureSwitching.getFeature(key).toRight(invalidFeature).right
    } yield Ok(Json.toJson(featureResponse(feature, featureSwitching.featureIsActive(feature))))).merge
  }

}
