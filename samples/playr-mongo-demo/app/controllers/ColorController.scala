package controllers

import play.api.libs.json.Json
import twentysix.playr.mongo.MongoCrudController
import play.api.mvc.EssentialAction
import play.api.libs.json.JsObject
import reactivemongo.bson.BSONObjectID
import play.api.mvc.Action
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.JsValue

case class Color(name: String, rgb: String, _id: Option[JsValue])

object Color {
  implicit def format = Json.format[Color]
}

object ColorController extends MongoCrudController[Color] {
  val collectionName = "color"

  def selectorFromId(id: BSONObjectID): JsObject = idSelector(id)

  def list = Action.async { request =>
    listFromCollection(Json.obj()).map(Ok(_))
  }

  def read(selector: JsObject, resource: Color) = Action {
    Ok(Json.toJson(resource))
  }

  def create = Action.async(parse.json) { implicit request =>
    withValidBodyAsync[Color]{ color =>
      insertInCollection(color) ifSuccess { Created(_) }
    }
  }

  def write(selector: JsObject, resource: Color) = Action.async(parse.json) { implicit request =>
    withValidBodyAsync[Color] { newColor =>
      updateCollection(selector, newColor) ifSuccess { value =>
        Ok(Json.toJson(newColor))
      }
    }
  }

  def delete(selector: JsObject, resource: Color) = Action {
    collection.remove(selector)
    NoContent
  }
}