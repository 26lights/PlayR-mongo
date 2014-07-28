package twentysix.playr.mongo

import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.__
import play.api.mvc.Controller
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.JsValue
import scala.concurrent.Future
import play.api.mvc.SimpleResult
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits.defaultContext

trait ResourceShortcuts {
  this: Controller =>
  def idSelector(bid: BSONObjectID) = Json.obj("_id" -> bid)

  def jsonRemoveId =  ( __ \ '_id ).json.prune

  def jsonGenerateId = __.json.update((__ \ '_id).json.put(BSONFormats.BSONObjectIDFormat.writes(BSONObjectID.generate)))

  implicit class FutureMongoResult(result: Future[Either[LastError, JsValue]]) {
    def ifSuccess(block: JsValue => SimpleResult): Future[SimpleResult] = {
      result.map{
        case Right(value) => block(value)
        case Left(error) => InternalServerError(error.stringify)
      }
    }

    def ifSuccessAsync(block: JsValue => Future[SimpleResult]): Future[SimpleResult] = {
      result.flatMap{
        case Right(value) => block(value)
        case Left(error) => Future.successful(InternalServerError(error.stringify))
      }
    }
}
}