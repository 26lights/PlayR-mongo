package twentysix.playr.mongo

import scala.concurrent.Future
import scala.language.implicitConversions
import reflect.runtime.universe.TypeTag

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{EssentialAction, Request, SimpleResult}
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.BSONFormats.BSONObjectIDFormat
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.LastError
import scala.collection
import scala.collection

abstract class MongoResource[R:Format] extends Resource[R] {
  val collectionName: String

  def name = collectionName

  implicit def collection: JSONCollection =  db.collection[JSONCollection](collectionName)

  def parseId(sid: String) = BSONObjectID.parse(sid).toOption.map(selectorFromId)

  def selectorFromId(id: BSONObjectID): JsObject

  def idSelector(bid: BSONObjectID):JsObject = idSelector(Json.toJson(bid))

  def jsonIdGenerator = BSONFormats.BSONObjectIDFormat.writes(BSONObjectID.generate)

  def resourceFromSelector(selector: JsObject) = collection.find(selector).one[R]

  def resourcesFromCollection(selector: JsObject): Future[Seq[R]] = collection.find(selector).cursor[R].collect[Seq]()

  def listFromCollection(selector: JsObject): Future[JsValue] = resourcesFromCollection(selector).map { list =>
      Json.toJson(list)
  }

  def updateCollection(selector: JsValue, value: JsValue): Future[Either[LastError, JsValue]] = super.updateCollection(selector, value)(collection)

  def insertInCollection(value: JsValue): Future[Either[LastError, JsValue]] = super.insertInCollection(value)(collection)

  def insertInCollection(value: R): Future[Either[LastError, JsValue]] =
    insertInCollection(Json.toJson(value))

  def updateCollection(selector: JsValue, value: R): Future[Either[LastError, JsValue]] =
    updateCollection(selector, Json.toJson(value))

}


abstract class MongoReadController[R:Format] extends MongoResource[R]
                                                with ResourceRead

abstract class MongoRwController[R:Format] extends MongoReadController[R]
                                              with ResourceRead
                                              with ResourceUpdate
                                              with ResourceCreate

abstract class MongoCrudController[R:Format] extends MongoReadController[R]
                                                with ResourceWrite
                                                with ResourceDelete
                                                with ResourceCreate
