package com.gu.featureswitching.play

import play.api.mvc._
import play.api.libs.json._
import com.gu.featureswitching.responses._
import com.gu.featureswitching.{play => _, _}


class DefaultPlayHandler(val features: List[FeatureSwitch],
                         val request: Request[AnyContent])
    extends PlayFeatureApi with PlayHandler with InMemoryFeatureSwitchEnablingStrategy
    with PlayCookieOverrideStrategy

trait PlayFeatureApi {

  val features: List[FeatureSwitch]

  def getHandler(req: Request[AnyContent]): PlayHandler =
    new DefaultPlayHandler(features, req)

  def PlayFeatureAction(f: (Request[AnyContent], FeatureSwitching) => Result) =
    Action { request =>
      val handler = getHandler(request)
      handler(f(request, handler))
    }
}

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

// trait PlayFeaturesApi extends Controller with FeatureSwitching with FeaturesApi {
//   val features: List[FeatureSwitch]

//   implicit val stringEntitySerializer = Json.writes[StringEntity] 
//   implicit val errorSerializer = Json.writes[ErrorEntity] 
//   implicit val booleanEntitySerializer = Json.writes[BooleanEntity]

//   implicit val featuresSerializer = Json.writes[FeatureSwitch] 
//   implicit val toggleResponseSerializer = Json.writes[ToggleResponse] 
//   implicit val featureSwitchEntitySerializer = Json.writes[FeatureSwitchEntity] 
//   implicit val featureSwitchResponseSerializer = Json.writes[FeatureSwitchResponse] 

//   implicit val featureSwitchIndexResponseSerializer = Json.writes[FeatureSwitchIndexResponse]
//   implicit val featureSwitchRootSerializer = Json.writes[FeatureSwitchRoot] 
//   implicit val featureSwitchRootResponseSerializer = Json.writes[FeatureSwitchRootResponse] 

//   def healthCheck = Action { 
//     Ok(Json.toJson(StringEntity("ok")))
//   }

//   def rootResponse = Action {
//     Ok(Json.toJson(FeatureSwitchRootResponse(FeatureSwitchRoot(
//       FeatureSwitchIndexResponse(Some(switchesUri), featuresResponses)
//     ))))    
//   }

//   def featureList = Action {
//     Ok(Json.toJson(FeatureSwitchIndexResponse(None, featuresResponses))) 
//   }

//   def featureByKey(key: String) = Action {
//     getFeature(key).fold(
//       NotFound(Json.toJson(ErrorEntity("invalid-feature")))
//     )(feature => {
//       Ok(Json.toJson(featureResponse(feature))) 
//     })
//   }

//   def featureEnabledByKey(key: String) = Action {
//     getFeature(key).fold(
//       NotFound(Json.toJson(ErrorEntity("invalid-feature")))
//     )(feature => {
//       featureIsEnabled(feature).fold(
//         NotFound(Json.toJson(ErrorEntity("unset-feature")))
//       )(enabled => { 
//         Ok(Json.toJson(BooleanEntity(enabled)))
//       })
//     }) 
//   }
// }
