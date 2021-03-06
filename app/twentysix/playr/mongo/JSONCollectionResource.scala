package twentysix.playr.mongo

import scala.annotation.implicitNotFound
import scala.concurrent.Future
import scala.language.implicitConversions

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult


abstract class JSONCollectionResource[R:Format, I: JSONCollectionIdProvider] extends Resource[R] {
  val collectionName: String
  val idProvider = implicitly[JSONCollectionIdProvider[I]]

  def name = collectionName

  implicit def collection: JSONCollection =  reactiveMongoApi.db.collection[JSONCollection](collectionName)

  def parseId(sid: String) = idProvider.parse(sid).map(selectorFromId)

  def selectorFromId(id: I): JsObject

  def idSelector(id: I): JsObject = idSelector(idProvider.toJson(id))

  def jsonIdGenerator = idProvider.toJson(idProvider.generate)

  def resourceFromSelector(selector: JsObject) = collection.find(selector).one[R]

  def resourceFromId(id: I) = collection.find(idSelector(id)).one[R]

  def resourceFromStringId(sid: String): Future[Option[R]] = parseId(sid).map( resourceFromSelector ).getOrElse(Future.successful(None))

  def resourcesFromCollection(selector: JsObject): Future[Seq[R]] = collection.find(selector).cursor[R]().collect[Seq]()

  def listFromCollection(selector: JsObject): Future[JsValue] = resourcesFromCollection(selector).map { list =>
      Json.toJson(list)
  }

  def updateCollection(selector: JsObject, value: JsValue): Future[Either[WriteResult, JsValue]] = super.updateCollection(selector, value)(collection)

  def insertInCollection(value: JsValue): Future[Either[WriteResult, JsValue]] = super.insertInCollection(value)(collection)

  def insertInCollection(value: R): Future[Either[WriteResult, JsValue]] =
    insertInCollection(Json.toJson(value))

  def updateCollection(selector: JsObject, value: R): Future[Either[WriteResult, JsValue]] =
    updateCollection(selector, Json.toJson(value))
}
