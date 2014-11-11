import org.specs2.mutable._

import scala.concurrent.Future

import play.api.mvc._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._

import com.gu.featureswitching.FeatureSwitch
import com.gu.featureswitching.play._

class ExampleSpec extends Specification {
  val emptyFeatures = new FeaturesApi {
    val features = List()
  }

  val fakeFeatures = new FeaturesApi {
    val features = List(
      FeatureSwitch("featureOn", "Feature On", true),
      FeatureSwitch("featureOff", "Feature Off", false)
    )
  }

  "FeaturesApi featureList" should {
    "return a list of features" in {
      running(FakeApplication()) {
        val result: Future[Result] = fakeFeatures.featureList().apply(FakeRequest())
        val bodyJson: JsValue = contentAsJson(result)

println(bodyJson)
false
        val expectedJson: JsValue = Json.parse("""[{"key":"featureOn","title":"Feature On","default":true},{"key":"featureOff","title":"Feature Off","default":false}]""")

        bodyJson must be equalTo expectedJson
      }
    }
  }

  "FeaturesApi healthCheck" should {
    "respond 'ok'" in {
      running(FakeApplication()) {
        val result: Future[Result] = emptyFeatures.healthCheck().apply(FakeRequest())
        val bodyText: String = contentAsString(result)

        bodyText must be equalTo "ok"
      }
    }
  }
}
