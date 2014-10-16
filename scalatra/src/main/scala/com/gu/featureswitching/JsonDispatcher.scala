package com.gu.featureswitching.dispatcher

import net.liftweb.json._
import org.scalatra.{ScalatraServlet, RenderPipeline}

trait JsonDispatcher extends ScalatraServlet {

  override protected def renderPipeline = ({
    case p: Product => {
      implicit val formats = DefaultFormats
      contentType = "application/json; charset=utf-8"
      val decomposed = Extraction.decompose(p)
      val rendered = JsonAST.render(decomposed)
      net.liftweb.json.compact(rendered).getBytes("UTF-8")
    }
  }: RenderPipeline) orElse super.renderPipeline

}
