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

abstract class MongoResource[R:Format] extends Resource[BSONObjectID, R] {
  val collectionName: String

  def name = collectionName

  def collection: JSONCollection =  db.collection[JSONCollection](collectionName)

  def parseId(sid: String) = BSONObjectID.parse(sid).toOption

  def resourceFromSelector(selector: JsObject) = collection.find(selector).one[R]

  def listFromCollection(selector: JsObject): Future[JsValue] = collection.find(selector).cursor[R].collect[Seq]().map { list =>
      Json.toJson(list)
  }

  def insertInCollection(value: R): Future[Either[LastError, JsValue]] =
    insertInCollection(Json.toJson(value))

  /**
   * Assign an _id to a new json document and insert it in the resource's collection.
   */
  def insertInCollection(value: JsValue): Future[Either[LastError, JsValue]] = {
    val newValue = value.transform(jsonGenerateId).get
    collection.insert(newValue).map { lastError =>
      if(lastError.ok) Right(newValue)
      else Left(lastError)
    }
  }

  def updateCollection(selector: JsValue, value: R): Future[Either[LastError, JsValue]] =
    updateCollection(selector, Json.toJson(value))

  /**
   * Remove _id from value and update the corresponding document in the resource's collection.
   */
  def updateCollection(selector: JsValue, value: JsValue): Future[Either[LastError, JsValue]] = {
    val newValue = value.transform(jsonRemoveId).get
    collection.update(selector, newValue).map { lastError =>
      if(lastError.ok) Right(newValue)
      else Left(lastError)
    }
  }
}

object MongoResource {
  implicit def mongoIdResourceAction[R, C<:MongoResource[R]](f: (JsObject, R)=> EssentialAction)(implicit tt: TypeTag[(JsObject, R)=> EssentialAction]) =
    Resource.mongoResourceAction[BSONObjectID, R, C](f)
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
