package com.gu.featureswitching.play

import play.api.mvc.{ Request, AnyContent }
import com.gu.featureswitching.{ FeatureState, FeatureSwitch }

class PlayFeatureState(
  val features: List[FeatureSwitch],
  val request: Request[AnyContent] 
) extends FeatureState
