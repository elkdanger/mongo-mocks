package org.elkdanger.testing

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.CollectionIndexesManager
import reactivemongo.api.{CollectionProducer, FailoverStrategy, DefaultDB}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.Manifest

trait MongoMocks extends MockitoSugar
  with MongoMockExtensions {

  implicit val mockMongoApi = mock[ReactiveMongoApi]
  implicit val mockMongoDb = mock[DefaultDB]

  when(mockMongoApi.database).thenReturn(Future.successful(mockMongoDb))

  import scala.concurrent.ExecutionContext.Implicits.global

  object MockCollection {
    def apply(name: Option[String] = None) = new MockCollectionBuilder(MockCollectionBuilder.buildMockCollection(name))
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

    def setupFind[T](returns: Traversable[T])(implicit manifest: Manifest[T]) = collection.setupFind(returns)

    def setupFind[T](filter: JsObject, returns: Traversable[T])(implicit manifest: Manifest[T]) = collection.setupFind(filter, returns)

    def setupFind[T](returns: Option[T])(implicit manifest: Manifest[T]) = collection.setupFind(returns)

    def setupFind[T](filter: JsObject, returns: Option[T])(implicit manifest: Manifest[T]) = collection.setupFind(filter, returns)

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

  object MockCollectionBuilder {
    /*
     * Builds a new mock JSONCollection
     */
    def buildMockCollection(name: Option[String] = None)(implicit db: DefaultDB, ec: ExecutionContext): JSONCollection = {

      val collection = mock[JSONCollection]

      val matcher = name match {
        case Some(x) => eqTo(x)
        case _ => any()
      }

      when(db.collection(matcher, any[FailoverStrategy])(any[CollectionProducer[JSONCollection]]()))
        .thenReturn(collection)

      val mockIndexManager = mock[CollectionIndexesManager]
      when(mockIndexManager.ensure(any())).thenReturn(Future.successful(true))
      when(collection.indexesManager).thenReturn(mockIndexManager)

      collection
    }
  }

  implicit def collectionConversion(builder: MockCollectionBuilder): JSONCollection = builder.collection

}
