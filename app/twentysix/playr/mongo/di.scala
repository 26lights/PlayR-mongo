package twentysix.playr.mongo

import javax.inject.Inject
import akka.stream.Materializer
import play.modules.reactivemongo.ReactiveMongoApi

object di {
  class MongoResourceConfig @Inject() (val reactiveMongoApi: ReactiveMongoApi, val materializer: Materializer) extends MongoResourceConfigTrait {
  }

}
