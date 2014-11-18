import com.gu.featureswitching.play.PlayFeatureState
import com.gu.featureswitching.{ FeatureStrategy, FeatureSwitch }

object PlayCookieFeatureStrategy extends FeatureStrategy[PlayFeatureState] {
  val name = "play-cookie"

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
