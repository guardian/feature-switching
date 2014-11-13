import org.specs2.mutable._
import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import com.gu.featureswitching.FeatureSwitch
import com.gu.featureswitching.play._
import scala.io.Source

class PlayFeaturesApiSpec extends Specification {
  def getJsonFixture(filename: String): JsValue = {
    Json.parse(
      Source.fromInputStream(
        this.getClass.getClassLoader.getResourceAsStream(s"fixtures/$filename.json")
      ).mkString
    )
  }

  class TestFeature extends PlayFeaturesApi {
    val features: List[FeatureSwitch] = List()

    // Members declared in com.gu.featureswitching.FeatureSwitchingEnablingStrategy
    def featureIsEnabled(feature: com.gu.featureswitching.FeatureSwitch): Option[Boolean] = { Option(false) }
    def featureResetEnabled(feature: com.gu.featureswitching.FeatureSwitch): Unit = { Unit }
    def featureSetEnabled(feature: com.gu.featureswitching.FeatureSwitch,enabled: Boolean): Unit = { Unit }

    // Members declared in com.gu.featureswitching.FeatureSwitchingOverrideStrategy
    def featureIsOverridden(feature: com.gu.featureswitching.FeatureSwitch): Option[Boolean] = { Option(false) }
    def featureResetOverride(feature: com.gu.featureswitching.FeatureSwitch): Unit = { Unit }
    def featureSetOverride(feature: com.gu.featureswitching.FeatureSwitch,overridden: Boolean): Unit = { Unit }

    // Members declared in com.gu.featureswitching.FeaturesApi
    def baseApiUri:String = "root"
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

  "featureEnabledByKey (GET api/features/switches/:key/enabled)" should {
    "when feature unavailable" >> {
      "return 404, with body 'invalid-feature'" >> {
        running(FakeApplication()) {
          val subject =  new TestEmptyFeatures 
          val result: Future[Result] = subject.featureEnabledByKey("featureOn").apply(FakeRequest())
          val expectedJson: JsValue = Json.parse("""
            {
              "errorKey":"invalid-feature"
            }
          """)

          contentAsJson(result) must be equalTo expectedJson 
          status(result) must be equalTo 404 
        }
      }
    }

    "when feature unset" >> {
      "return 404, with body 'unset-feature'" >> {
        running(FakeApplication()) {
          val subject =  new TestUnsetFeatures 
          val result: Future[Result] = subject.featureEnabledByKey("featureOn").apply(FakeRequest())
          val expectedJson: JsValue = Json.parse("""
            {
              "errorKey":"unset-feature"
            }
          """)

          contentAsJson(result) must be equalTo expectedJson 
          status(result) must be equalTo 404 
        }
      }
    }

    "when feature available" >> {
      "return 200, with json value" >> {
        running(FakeApplication()) {
          val subject =  new TestEnabledFeatures
          val result: Future[Result] = subject.featureEnabledByKey("featureOn").apply(FakeRequest())
          val expectedJson: JsValue = Json.parse("""
            {
              "data":true
            }
          """)

          contentAsJson(result) must be equalTo expectedJson 
          status(result) must be equalTo 200 
        }
      }
    }
  }

  "featureByKey (GET api/features/switches/:key)" should {
    "when feature available" >> {
      "return 200, with json value" >> {
        running(FakeApplication()) {
          val subject =  new TestFeatures
          val result: Future[Result] = subject.featureByKey("featureOn").apply(FakeRequest())

          contentAsJson(result) must be equalTo getJsonFixture("feature") 
          status(result) must be equalTo 200 
        }
      }
    }

    "when feature unavailable" >> {
      "return 404, with body 'invalid-feature'" >> {
        running(FakeApplication()) {
          val subject =  new TestEmptyFeatures
          val result: Future[Result] = subject.featureByKey("featureOn").apply(FakeRequest())
          val expectedJson: JsValue = Json.parse("""
            {
              "errorKey":"invalid-feature"
            }
          """)

          contentAsJson(result) must be equalTo expectedJson
          status(result) must be equalTo 404 
        }
      }
    }
  }

  "featureList (GET api/features/switches)" should {
    "when feature list not-empty" >> {
      "return 200, with json list of features" in {
        running(FakeApplication()) {
          val subject = new TestFeatures
          val result: Future[Result] = subject.featureList.apply(FakeRequest())
          
          status(result) must be equalTo 200
          contentAsJson(result) must be equalTo getJsonFixture("feature_list") 
        }
      }
    }

    "when feature list empty" >> {
      "return 200, with an empty json list" in {
        running(FakeApplication()) {
          val subject = new TestEmptyFeatures
          val result: Future[Result] = subject.featureList.apply(FakeRequest())
          val expectedJson: JsValue = Json.parse("""
            {
              "data":[]
            }
          """)

          status(result) must be equalTo 200
          contentAsJson(result) must be equalTo expectedJson
        }
      }
    }
  }

  "rootResponse (GET api/features)" should {
    "return 200, with json response" in {
      running(FakeApplication()) {
        val subject = new TestFeatures
        val result: Future[Result] = subject.rootResponse.apply(FakeRequest())

        status(result) must be equalTo 200
        contentAsJson(result) must be equalTo getJsonFixture("root") 
      }
    }
  }

  "healthCheck" should {
    "return 200, with json ok response" in {
      running(FakeApplication()) {
        val subject = new TestEmptyFeatures
        val result: Future[Result] = subject.healthCheck.apply(FakeRequest())
        val expectedJson: JsValue = Json.parse("""
          {
            "data": "ok"
          }
        """)

        status(result) must be equalTo 200
        contentAsJson(result) must be equalTo expectedJson
      }
    }
  }
}
