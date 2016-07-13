package org.elkdanger.testing

import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.DefaultDB
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

trait MongoMocks extends MockitoSugar
  with MongoMockFinds
  with MongoMockInserts
  with MongoMockUpdates
  with MongoMockCollections {

  implicit val mockMongoApi = mock[ReactiveMongoApi]
  implicit val mockMongoDb = mock[DefaultDB]

  when(mockMongoApi.database).thenReturn(Future.successful(mockMongoDb))

  import scala.concurrent.ExecutionContext.Implicits.global

  object MockCollection {
    def apply(name: Option[String] = None) = new MockCollectionBuilder(mockCollection(name))
  }

  class MockCollectionBuilder(val collection: JSONCollection) {

    private var filter: Option[Seq[(String, JsValueWrapper)]] = None

    def ? (filter: (String, JsValueWrapper)*) = {
      this.filter = Some(filter)
      this
    }

    def ~> [T](obj: Option[T])(implicit manifest: Manifest[T]): Unit = {
      filter match {
        case Some(_) => collection.setupFind(filter: JsObject, obj)
        case _ => collection.setupFind(obj)
      }
    }

    def ~> [T](seq: Traversable[T])(implicit manifest: Manifest[T]): Unit = {
      filter match {
        case Some(_) => collection.setupFind(filter: JsObject, seq)
        case _ => collection.setupFind(seq)
      }
    }

    /*
     * Implicit conversion between a sequence of tuples to a JsObject
     */
    private implicit def jsonObjWrapper(f: Option[Seq[(String, JsValueWrapper)]]): JsObject =
      f match {
        case Some(x) => Json.obj(x:_*)
        case _ => Json.obj()
      }

  }

  implicit def collectionConversion(builder: MockCollectionBuilder): JSONCollection = builder.collection

}
