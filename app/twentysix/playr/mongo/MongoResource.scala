package twentysix.playr.mongo

import scala.annotation.implicitNotFound
import scala.language.implicitConversions

import play.api.libs.json.Format
import reactivemongo.bson.BSONObjectID

abstract class MongoResource[R:Format] extends JSONCollectionResource[R, BSONObjectID]


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
