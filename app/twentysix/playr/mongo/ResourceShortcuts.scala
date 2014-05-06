package twentysix.playr.mongo

import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.__
import play.api.mvc.Controller
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
import reactivemongo.bson.BSONObjectID

trait ResourceShortcuts {
  this: Controller =>
  def idSelector(bid: BSONObjectID) = Json.obj("_id" -> bid)

  def jsonRemoveId =  ( __ \ '_id ).json.prune

  def jsonGenerateId = __.json.update((__ \ '_id).json.put(BSONFormats.BSONObjectIDFormat.writes(BSONObjectID.generate)))
}