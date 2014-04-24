package twentysix.playr.mongo

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsObject
import play.api.mvc.{Action, EssentialAction}
import play.modules.reactivemongo.MongoController
import twentysix.playr.core
import reactivemongo.bson.BSONObjectID

trait BaseResource extends core.BaseResource with MongoController{
  type ResourceType

  def handleAction(id: IdentifierType, f: Function2[JsObject, ResourceType, EssentialAction]) = {
    val selector = selectorFromId(id)
    val action = EssentialAction { rh =>
      Iteratee.flatten {
        resourceFromSelector(selector).map { resource =>
          resource.map(f(selector, _)(rh)).getOrElse(Action{NotFound}(rh))
        }
      }
    }
    Some(action)
  }

  def selectorFromId(id: IdentifierType): JsObject

  def resourceFromSelector(selector: JsObject): Future[Option[ResourceType]]
}

trait Resource[I, R] extends BaseResource with core.ResourceTrait[I] {
  type ResourceType = R
}

object Resource {
}

trait IdResource[R] extends Resource[BSONObjectID, R]


trait ResourceRead extends core.ResourceRead {
  this: BaseResource =>
  def readResource(id: IdentifierType) = handleAction(id, read)
  def listResource = Some(list)

  def read(selector: JsObject, resource: ResourceType): EssentialAction
  def list: EssentialAction
}

trait ResourceWrite extends core.ResourceWrite{
  this: BaseResource =>
  def writeResource(id: IdentifierType) = handleAction(id, write)

  def write(selector: JsObject, resource: ResourceType): EssentialAction
}

trait ResourceDelete extends core.ResourceDelete {
  this: BaseResource =>
  def deleteResource(id: IdentifierType) = handleAction(id, delete)

  def delete(selector: JsObject, resource: ResourceType): EssentialAction
}

trait ResourceUpdate extends core.ResourceUpdate {
  this: BaseResource =>
  def updateResource(id: IdentifierType) = handleAction(id, update)

  def update(selector: JsObject, resource: ResourceType): EssentialAction
}

trait ResourceCreate extends core.ResourceCreate {
  this: BaseResource =>
  def createResource = Some(create)

  def create: EssentialAction
}

