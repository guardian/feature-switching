import org.specs2.mutable._
import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import com.gu.featureswitching.{ FeatureSwitch, FeatureStrategy, InMemoryFeatureSwitchEnablingStrategy, FeatureState }
import com.gu.featureswitching.responses.{ BooleanEntity, StringEntity }
import com.gu.featureswitching.play._
import scala.io.Source

class PlayFeaturesApiSpec extends Specification {
  implicit val booleanEntitySerializer = Json.writes[BooleanEntity]
  implicit val stringEntitySerializer = Json.writes[StringEntity]

  def getJsonFixture(filename: String): JsValue = {
    Json.parse(
      Source.fromInputStream(
        this.getClass.getClassLoader.getResourceAsStream(s"fixtures/$filename.json")
      ).mkString
    )
  }

  object TestFeatureStrategy extends FeatureStrategy[PlayFeatureState] {
    val name: String = "fake"
    def get(state: PlayFeatureState, feature: FeatureSwitch): Option[Boolean] = {
      Some(true)
    }
    def set(state: PlayFeatureState, feature: FeatureSwitch): PlayFeatureState = {
      state
    }
    def reset(state: PlayFeatureState, feature: FeatureSwitch): PlayFeatureState = {
      state
    }
  }

  class TestFeature extends PlayFeaturesApi { 
    type StateType = PlayFeatureState

    val features: List[FeatureSwitch] = List()
    val strategies: List[FeatureStrategy[PlayFeatureState]] = List()
    def baseApiUri:String = "root"
  }

  trait enableAllStrategy extends TestFeature {
    override val strategies: List[FeatureStrategy[PlayFeatureState]] = List(TestFeatureStrategy) 
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
  class TestFeaturesWithEnableAllStrategy extends TestFeature with simpleFeatures with enableAllStrategy 

  "getFeatureByKey (GET api/features/switches/:key)" should {
    "when enable-all strategy" >> {
      "return 200, with json value with features enabled" >> {
        running(FakeApplication()) {
          val subject =  new TestFeaturesWithEnableAllStrategy
          val result: Future[Result] = subject.getFeatureByKey("featureOff").apply(FakeRequest())

          contentAsJson(result) must be equalTo getJsonFixture("feature_enabled") 
          status(result) must be equalTo 200 
        }
      }
    }

    "when feature available" >> {
      "return 200, with json value" >> {
        running(FakeApplication()) {
          val subject =  new TestFeatures
          val result: Future[Result] = subject.getFeatureByKey("featureOn").apply(FakeRequest())

          contentAsJson(result) must be equalTo getJsonFixture("feature") 
          status(result) must be equalTo 200 
        }
      }
    }

    "when feature unavailable" >> {
      "return 404, with body 'invalid-feature'" >> {
        running(FakeApplication()) {
          val subject =  new TestEmptyFeatures
          val result: Future[Result] = subject.getFeatureByKey("featureOn").apply(FakeRequest())
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

  "getFeatureList (GET api/features/switches)" should {
    "when feature list not-empty" >> {
      "return 200, with json list of features" in {
        running(FakeApplication()) {
          val subject = new TestFeatures
          val result: Future[Result] = subject.getFeatureList.apply(FakeRequest())
          
          status(result) must be equalTo 200
          contentAsJson(result) must be equalTo getJsonFixture("feature_list") 
        }
      }
    }

    "when feature list empty" >> {
      "return 200, with an empty json list" in {
        running(FakeApplication()) {
          val subject = new TestEmptyFeatures
          val result: Future[Result] = subject.getFeatureList.apply(FakeRequest())
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

  "getRootResponse (GET api/features)" should {
    "return 200, with json response" in {
      running(FakeApplication()) {
        val subject = new TestFeatures
        val result: Future[Result] = subject.getRootResponse.apply(FakeRequest())

        status(result) must be equalTo 200
        contentAsJson(result) must be equalTo getJsonFixture("root") 
      }
    }
  }

  "getHealthCheck" should {
    "return 200, with json ok response" in {
      running(FakeApplication()) {
        val subject = new TestEmptyFeatures
        val result: Future[Result] = subject.getHealthCheck.apply(FakeRequest())
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
