import org.specs2.mutable._
import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import com.gu.featureswitching.FeatureSwitch
import com.gu.featureswitching.play._

class ExampleSpec extends Specification {
  class TestFeature extends FeaturesApi {
    val features: List[FeatureSwitch] = List()

    // Members declared in com.gu.featureswitching.FeatureSwitchingEnablingStrategy
    def featureIsEnabled(feature: com.gu.featureswitching.FeatureSwitch): Option[Boolean] = { Option(false) }
    def featureResetEnabled(feature: com.gu.featureswitching.FeatureSwitch): Unit = { Unit }
    def featureSetEnabled(feature: com.gu.featureswitching.FeatureSwitch,enabled: Boolean): Unit = { Unit }

    // Members declared in com.gu.featureswitching.FeatureSwitchingOverrideStrategy
    def featureIsOverridden(feature: com.gu.featureswitching.FeatureSwitch): Option[Boolean] = { Option(false) }
    def featureResetOverride(feature: com.gu.featureswitching.FeatureSwitch): Unit = { Unit }
    def featureSetOverride(feature: com.gu.featureswitching.FeatureSwitch,overridden: Boolean): Unit = { Unit }
  }

  trait enabledFeature extends TestFeature {
    override def featureIsEnabled(feature: com.gu.featureswitching.FeatureSwitch): Option[Boolean] = { Some(true) }
  }

  trait unavailableFeature extends TestFeature {
    override def featureIsEnabled(feature: com.gu.featureswitching.FeatureSwitch): Option[Boolean] = { None }
  }

  trait simpleFeatures extends TestFeature {
    override val features = List(
      FeatureSwitch("featureOn", "Feature On", true),
      FeatureSwitch("featureOff", "Feature Off", false)
    )
  }

  trait emptyFeatures extends TestFeature {
    override val features = List()
  }

  class TestFeatures extends TestFeature with simpleFeatures 
  class TestEmptyFeatures extends TestFeature with emptyFeatures
  class TestEnabledFeatures extends TestFeature with simpleFeatures with enabledFeature 
  class TestUnavailableFeatures extends TestFeature with simpleFeatures with unavailableFeature 

  "FeaturesApi featureEnabledByKey" should {
    "return the enabled state of a feature" in {

      "when feature unavailable returns 404" >> {
        running(FakeApplication()) {
          val subject =  new TestUnavailableFeatures 
          val result: Future[Result] = subject.featureEnabledByKey("featureOn").apply(FakeRequest())

          contentAsString(result) must be equalTo "unset-feature"
          status(result) must be equalTo 404 
        }
      }

      "when feature enabled returns value" >> {
        running(FakeApplication()) {
          val subject =  new TestEnabledFeatures
          val result: Future[Result] = subject.featureEnabledByKey("featureOn").apply(FakeRequest())
          val bodyJson: JsValue = contentAsJson(result)
          val expectedJson: JsValue = Json.parse("true")

          bodyJson must be equalTo expectedJson
        }
      }

    }
  }

  "FeaturesApi featureByKey" should {
    "return a feature" in {
      running(FakeApplication()) {
        val subject =  new TestFeatures
        val result: Future[Result] = subject.featureByKey("featureOn").apply(FakeRequest())
        val bodyJson: JsValue = contentAsJson(result)
        val expectedJson: JsValue = Json.parse("""{"key":"featureOn","title":"Feature On","default":true}""")

        bodyJson must be equalTo expectedJson
      }
    }
  }

  "FeaturesApi featureList" should {
    "return a list of features" in {
      running(FakeApplication()) {
        val subject = new TestFeatures
        val result: Future[Result] = subject.featureList().apply(FakeRequest())
        val bodyJson: JsValue = contentAsJson(result)
        val expectedJson: JsValue = Json.parse("""[{"key":"featureOn","title":"Feature On","default":true},{"key":"featureOff","title":"Feature Off","default":false}]""")

        bodyJson must be equalTo expectedJson
      }
    }
  }

  "FeaturesApi healthCheck" should {
    "respond 'ok'" in {
      running(FakeApplication()) {
        val subject = new TestEmptyFeatures
        val result: Future[Result] = subject.healthCheck().apply(FakeRequest())
        val bodyText: String = contentAsString(result)

        bodyText must be equalTo "ok"
      }
    }
  }
}
