package twentysix.playr.mongo

import reflect.runtime.universe.{Type,TypeTag,typeOf}
import scala.language.implicitConversions
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats
import play.api.libs.json._
import play.api.mvc.EssentialAction
import play.api.mvc.Action
import scala.concurrent.Future

abstract class MongoResource[R:Format] extends Resource[BSONObjectID, R] {
  def jsonRemoveId =  ( __ \ '_id ).json.prune

  def jsonGenerateId = __.json.update((__ \ '_id).json.put(BSONFormats.BSONObjectIDFormat.writes(BSONObjectID.generate)))

  def collection: JSONCollection

  def parseId(sid: String) = BSONObjectID.parse(sid).toOption

  def resourceFromSelector(selector: JsObject) = collection.find(selector).one[R]
}

object MongoResource {
  implicit def mongoIdResourceAction[R, C<:MongoResource[R]](f: (JsObject, R)=> EssentialAction)(implicit tt: TypeTag[(JsObject, R)=> EssentialAction]) =
    Resource.mongoResourceAction[BSONObjectID, R, C](f)
}
