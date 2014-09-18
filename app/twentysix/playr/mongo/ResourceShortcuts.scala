package twentysix.playr.mongo

import play.api.libs.json.{JsObject, Json, __, JsValue}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Controller
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future
import play.api.mvc.Result
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.collection.JSONCollection

trait ResourceShortcuts {
  this: Controller =>
  def idSelector(jid: JsValue) = Json.obj("_id" -> jid)

  def jsonRemoveId =  ( __ \ '_id ).json.prune

  def jsonGenerateId = __.json.update((__ \ '_id).json.put(jsonIdGenerator))

  def jsonIdGenerator:JsValue

  /**
   * Assign an _id to a new json document and insert it in the resource's collection.
   */
  def insertInCollection(value: JsValue)(implicit collection:JSONCollection): Future[Either[LastError, JsValue]] = {
    val newValue = value.transform(jsonGenerateId).get
    collection.insert(newValue).map { lastError =>
      if(lastError.ok) Right(newValue)
      else Left(lastError)
    }
  }

  /**
   * Remove _id from value and update the corresponding document in the resource's collection.
   */
  def updateCollection(selector: JsValue, value: JsValue)(implicit collection:JSONCollection): Future[Either[LastError, JsValue]] = {
    val newValue = value.transform(jsonRemoveId).get
    collection.update(selector, newValue).map { lastError =>
      if(lastError.ok) Right(newValue)
      else Left(lastError)
    }
  }

  implicit class FutureMongoResult(result: Future[Either[LastError, JsValue]]) {
    def ifSuccess(block: JsValue => Result): Future[Result] = {
      result.map{
        case Right(value) => block(value)
        case Left(error) => InternalServerError(error.stringify)
      }
    }

    def ifSuccessAsync(block: JsValue => Future[Result]): Future[Result] = {
      result.flatMap{
        case Right(value) => block(value)
        case Left(error) => Future.successful(InternalServerError(error.stringify))
      }
    }
  }
}