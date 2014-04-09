package twentysix.playr.mongo

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats
import play.api.libs.json._
import play.api.mvc.EssentialAction
import play.api.mvc.Action
import scala.concurrent.Future

trait MongoObject {
  val _id: Option[BSONObjectID]
}

abstract class MongoResource[R<:MongoObject:Format] extends Resource[BSONObjectID, R]
                                                       with ResourceRead
                                                       with ResourceCreate
                                                       with ResourceWrite
                                                       with ResourceDelete {
  def jsonRemoveId =  ( __ \ '_id ).json.prune

  def jsonGenerateId = __.json.update((__ \ '_id).json.put(BSONFormats.BSONObjectIDFormat.writes(BSONObjectID.generate)))

  def collection: JSONCollection

  def parseId(sid: String) = BSONObjectID.parse(sid).toOption

  def resourceFromSelector(selector: JsObject) = collection.find(selector).one

  def read[A](selector: JsObject, resource: ResourceType) = Action {
    Ok(Json.toJson(resource).transform(jsonRemoveId).get)
  }

  def list = Action.async {
    collection.find(Json.obj()).cursor[R].collect[Seq]().map { list =>
      Ok(Json.toJson(list))
    }
  }

  def write(selector: JsObject, resource: ResourceType) = Action.async(parse.json){ request =>
    request.body.validate[R] match {
      case value: JsSuccess[R] => {
        val result = Json.toJson(value.get).transform(jsonRemoveId).get
        collection.update(selector, result).map { error =>
          Ok(result)
        }
      }
      case e: JsError => Future(BadRequest(JsError.toFlatJson(e)))
    }
  }

  def delete(selector: JsObject, resource: ResourceType) = Action.async{ request =>
    collection.remove(selector).map { lastError =>
      NoContent
    }
  }


  def create: EssentialAction = Action.async(parse.json){ request =>
    request.body.validate[R] match {
      case value: JsSuccess[R] => {
        val result = Json.toJson(value.get).transform(jsonGenerateId).get
        collection.insert(result).map { error =>
          Ok(result)
        }
      }
      case e: JsError => Future(BadRequest(JsError.toFlatJson(e)))
    }
  }
}
