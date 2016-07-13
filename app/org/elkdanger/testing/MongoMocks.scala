package org.elkdanger.testing

import org.mockito.ArgumentCaptor
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
  with MongoMockExtensions
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

    def <~ [T](obj: T, fails: Boolean = false) = collection.setupInsert(obj, fails)

    def setupAnyInsert(fails: Boolean = false) = collection.setupAnyInsert(fails)

    def verifyAnyInsert = collection.verifyAnyInsert

    def verifyInsertWith[T](obj: T) = collection.verifyInsertWith(obj)

    def verifyInsertWith[T](captor: ArgumentCaptor[T]) = collection.verifyInsertWith(captor)

    def verifyUpdate[T](selectorFunc: (JsObject) => Unit = null, updateFunc: (JsObject) => Unit = null) = collection.verifyUpdate(selectorFunc, updateFunc)

    def verifyAnyUpdate[T] = collection.verifyAnyUpdate

    def setupUpdate[S, T](selector: S, obj: T, fails: Boolean = false) = collection.setupUpdate(selector, obj, fails)

    def setupAnyUpdate(fails: Boolean = false) = collection.setupAnyUpdate(fails)

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
