import org.specs2.mutable._
import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import com.gu.featureswitching.FeatureSwitch
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

  "putFeatureEnabledByKey (PUT api/features/switches/:key/enabled)" should {

    "when feature available" >> {
      "with invalid request json" >> {
        "return 400, with json error" >> {
          running(FakeApplication()) {
            val subject =  new TestFeatures
            val result: Future[Result] = subject.putFeatureEnabledByKey("featureOn").apply(
              FakeRequest("PUT", "root/features/switches/featureOn/enabled").withJsonBody(
                Json.toJson(StringEntity("foo"))
              )
            )
            val expectedJson: JsValue = Json.parse("""
              {
                "errorKey":"invalid-data"
              }
            """)

            contentAsJson(result) must be equalTo expectedJson 
            status(result) must be equalTo 400 
          }
        }
      }

      "with valid request json" >> {
        "return 200, with no content" >> {
          running(FakeApplication()) {
            val subject =  new TestFeatures
            val result: Future[Result] = subject.putFeatureEnabledByKey("featureOn").apply(
              FakeRequest("PUT", "root/features/switches/featureOn/enabled").withJsonBody(
                Json.toJson(BooleanEntity(true))
              )
            )

            contentAsString(result) must be equalTo "" 
            status(result) must be equalTo 200 
          }
        }
      }
    }


    "when feature unavailable" >> {
      "return 404, with json error" >> {
        running(FakeApplication()) {
          val subject =  new TestEmptyFeatures
          val result: Future[Result] = subject.putFeatureEnabledByKey("featureOn").apply(
            FakeRequest("PUT", "root/features/switches/featureOn/enabled").withJsonBody(
              Json.toJson(BooleanEntity(true))
            )
          )
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

    "when invalid json sent" >> {
      "return 400, with json error" >> {
        running(FakeApplication()) {
          val subject =  new TestUnsetFeatures 
          val result: Future[Result] = subject.putFeatureEnabledByKey("featureOn").apply(FakeRequest())
          val expectedJson: JsValue = Json.parse("""
            {
              "errorKey":"invalid-json"
            }
          """)

          contentAsJson(result) must be equalTo expectedJson 
          status(result) must be equalTo 400 
        }
      }
    }
  }

  "getFeatureEnabledByKey (GET api/features/switches/:key/enabled)" should {
    "when feature unavailable" >> {
      "return 404, with body 'invalid-feature'" >> {
        running(FakeApplication()) {
          val subject =  new TestEmptyFeatures 
          val result: Future[Result] = subject.getFeatureEnabledByKey("featureOn").apply(FakeRequest())
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
          val result: Future[Result] = subject.getFeatureEnabledByKey("featureOn").apply(FakeRequest())
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
          val result: Future[Result] = subject.getFeatureEnabledByKey("featureOn").apply(FakeRequest())
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
