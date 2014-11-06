package controllers

import twentysix.playr.RootApiRouter
import twentysix.playr.ApiInfo
import twentysix.playr.swagger.SwaggerRestDocumentation

object Application {
  val api = RootApiRouter()
    .add(ColorController)

  val apiInfo = ApiInfo(api)

  val apiDocs = new SwaggerRestDocumentation(api)
}