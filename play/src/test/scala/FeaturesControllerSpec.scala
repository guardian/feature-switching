import org.specs2.mutable._
import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import com.gu.featureswitching.{ FeatureSwitch, InMemoryFeatureSwitchEnablingStrategy }
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

  class TestFeature extends PlayFeaturesApi { 
    val features: List[FeatureSwitch] = List()
    def baseApiUri:String = "root"
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

  "getFeatureByKey (GET api/features/switches/:key)" should {

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
