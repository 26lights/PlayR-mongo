package twentysix.playr.mongo

import play.api.libs.json.JsObject
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.BSONFormats
import play.api.libs.json.JsValue

trait JSONCollectionIdProvider[I]{
  def parse(sid: String): Option[I]
  def toJson(id: I): JsValue
  def generate: I
}

object JSONCollectionIdProvider{
  implicit def providerBSONObjectID = new JSONCollectionIdProvider[BSONObjectID] {
    def parse(sid: String): Option[BSONObjectID] = BSONObjectID.parse(sid).toOption
    def toJson(id: BSONObjectID): JsValue = BSONFormats.BSONObjectIDFormat.writes(id)
    def generate: BSONObjectID = BSONObjectID.generate
  }
}
