package com.gu.featureswitching

import collection.JavaConverters._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.{AttributeValueUpdate, AttributeValue, UpdateItemRequest, ScanRequest}
import com.amazonaws.AmazonServiceException

trait DynamoDBFeatureSwitchingEnablingStrategy extends FeatureSwitching {
  @volatile var localCache = Map.empty[FeatureSwitch, Boolean]

  /** DynamoDB client with properly supplied credentials */
  protected val dynamoDBClient: AmazonDynamoDB

  protected val SwitchesTableName = "switches"
  protected val SwitchKeyName = "Switch Name"
  protected val SwitchValueName = "Switch Value"

  /** Consumer can use this to log Amazon errors */
  protected def onAmazonServiceError(error: AmazonServiceException): Unit

  /** Consumer should implement this so we can log warnings */
  protected def logWarn(s: String)

  private def withAmazonErrorRecovery(block: => Unit): Unit = {
    try {
      block
    } catch {
      case error: AmazonServiceException => onAmazonServiceError(error)
    }
  }

  /** A scheduler should regularly call this method (say once per minute) to keep the app in sync with the S3 JSON
    * document.
    */
  def triggerUpdate(): Unit = {
    withAmazonErrorRecovery {
      val result = dynamoDBClient.scan(new ScanRequest(SwitchesTableName))

      val dynamoDBSwitches = (result.getItems.asScala.toList flatMap { switches =>
        val scalaMap = switches.asScala
        for {
          name <- scalaMap.get(SwitchKeyName)
          value <- scalaMap.get(SwitchValueName)
          featureSwitch <- features.find(_.key == name)
        } yield featureSwitch -> (value.getS == "true")
      }).toMap

      val missingKeys = dynamoDBSwitches.keySet.diff(features.toSet)

      if (missingKeys.size > 0) {
        logWarn("Could not load following feature switches from DynamoDB: " + 
          missingKeys.map(_.key).toList.sorted.mkString(", "))
      }

      localCache ++= dynamoDBSwitches
    }
  }

  def featureIsEnabled(feature: FeatureSwitch): Option[Boolean] = localCache.get(feature)

  private def StringAttributeValue(keyName: String, value: String): AttributeValue = {
    val attribute = new AttributeValue(keyName)
    attribute.setS(value)
    attribute
  }

  def featureSetEnabled(feature: FeatureSwitch, enabled: Boolean): Unit = {
    withAmazonErrorRecovery {
      dynamoDBClient.updateItem(
        new UpdateItemRequest(
          SwitchesTableName,
          Map(SwitchKeyName -> StringAttributeValue(SwitchKeyName, feature.key)).asJava,
          Map(SwitchValueName -> new AttributeValueUpdate(StringAttributeValue(SwitchValueName, enabled.toString), "PUT")).asJava
      ))

      localCache += (feature -> enabled)
    }
  }

  def featureResetEnabled(feature: FeatureSwitch): Unit = featureSetEnabled(feature, feature.default)
}
