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
  class TestUnsetFeatures extends TestFeature with simpleFeatures with unavailableFeature 

  "FeaturesApi featureEnabledByKey" should {
    "when feature unavailable" >> {
      "return 404, with body 'invalid-feature'" >> {
        running(FakeApplication()) {
          val subject =  new TestEmptyFeatures 
          val result: Future[Result] = subject.featureEnabledByKey("featureOn").apply(FakeRequest())

          contentAsString(result) must be equalTo "invalid-feature"
          status(result) must be equalTo 404 
        }
      }
    }

    "when feature unset" >> {
      "return 404, with body 'unset-feature'" >> {
        running(FakeApplication()) {
          val subject =  new TestUnsetFeatures 
          val result: Future[Result] = subject.featureEnabledByKey("featureOn").apply(FakeRequest())

          contentAsString(result) must be equalTo "unset-feature"
          status(result) must be equalTo 404 
        }
      }
    }

    "when feature available" >> {
      "return 200, with json value" >> {
        running(FakeApplication()) {
          val subject =  new TestEnabledFeatures
          val result: Future[Result] = subject.featureEnabledByKey("featureOn").apply(FakeRequest())
          val bodyJson: JsValue = contentAsJson(result)
          val expectedJson: JsValue = Json.parse("true")

          bodyJson must be equalTo expectedJson
          status(result) must be equalTo 200 
        }
      }
    }
  }

  "FeaturesApi featureByKey" should {
    "when feature available" >> {
      "return 200, with json value" >> {
        running(FakeApplication()) {
          val subject =  new TestFeatures
          val result: Future[Result] = subject.featureByKey("featureOn").apply(FakeRequest())
          val bodyJson: JsValue = contentAsJson(result)
          val expectedJson: JsValue = Json.parse("""{"key":"featureOn","title":"Feature On","default":true}""")

          bodyJson must be equalTo expectedJson
          status(result) must be equalTo 200 
        }
      }
    }

    "when feature unavailable" >> {
      "return 404, with body 'invalid-feature'" >> {
        running(FakeApplication()) {
          val subject =  new TestEmptyFeatures
          val result: Future[Result] = subject.featureByKey("featureOn").apply(FakeRequest())

          contentAsString(result) must be equalTo "invalid-feature"
          status(result) must be equalTo 404 
        }
      }
    }
  }

  "FeaturesApi featureList" should {
    "when feature list not-empty" >> {
      "return 200, with json list of features" in {
        running(FakeApplication()) {
          val subject = new TestFeatures
          val result: Future[Result] = subject.featureList().apply(FakeRequest())
          val bodyJson: JsValue = contentAsJson(result)
          val expectedJson: JsValue = Json.parse("""[{"key":"featureOn","title":"Feature On","default":true},{"key":"featureOff","title":"Feature Off","default":false}]""")
          
          status(result) must be equalTo 200
          bodyJson must be equalTo expectedJson
        }
      }
    }

    "when feature list empty" >> {
      "return 200, with an empty json list" in {
        running(FakeApplication()) {
          val subject = new TestEmptyFeatures
          val result: Future[Result] = subject.featureList().apply(FakeRequest())
          val bodyJson: JsValue = contentAsJson(result)
          val expectedJson: JsValue = Json.parse("""[]""")

          status(result) must be equalTo 200
          bodyJson must be equalTo expectedJson
        }
      }
    }
  }

  "FeaturesApi healthCheck" should {
    "return 200, with body 'ok'" in {
      running(FakeApplication()) {
        val subject = new TestEmptyFeatures
        val result: Future[Result] = subject.healthCheck().apply(FakeRequest())
        val bodyText: String = contentAsString(result)

        bodyText must be equalTo "ok"
      }
    }
  }
}
