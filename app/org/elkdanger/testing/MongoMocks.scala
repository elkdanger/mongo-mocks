package org.elkdanger.testing

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.DefaultDB
import reactivemongo.api.commands._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

trait MongoMocks extends MockitoSugar
  with MongoMockFinds
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

    def <~ [T](obj: T, fails: Boolean = false) = {
      val m = mockWriteResult(fails)

      when(collection.insert(eqTo(obj), any())(any(), any()))
        .thenReturn(Future.successful(m))

      this
    }

    def setupAnyInsert(fails: Boolean = false) = {
      val m = mockWriteResult(fails)

      when(collection.insert(any(), any())(any(), any()))
        .thenReturn(Future.successful(m))

      this
    }

    def verifyAnyInsert = {
      verify(collection).insert(any, any())(any(), any())
    }

    def verifyInsertWith[T](obj: T) = {
      verify(collection).insert(eqTo(obj), any())(any(), any())
    }

    def verifyInsertWith[T](captor: ArgumentCaptor[T]) = {
      verify(collection).insert(captor.capture(), any[WriteConcern])(any(), any[ExecutionContext])
    }

    def verifyUpdate[T](filter: (JsObject) => Unit = null, update: (JsObject) => Unit = null) = {
      val filterCaptor = ArgumentCaptor.forClass(classOf[JsObject])
      val updaterCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      verify(collection).update(filterCaptor.capture(), updaterCaptor.capture(), any[WriteConcern], anyBoolean(), anyBoolean())(any(), any(), any[ExecutionContext])

      if (filter != null)
        filter(filterCaptor.getValue)

      if (update != null)
        update(updaterCaptor.getValue)
    }

    def verifyAnyUpdate[T] = {
      verify(collection).update(any(), any(), any[WriteConcern], anyBoolean(), anyBoolean())(any(), any(), any[ExecutionContext])
    }

    def setupUpdate[S, T](selector: S, obj: T, fails: Boolean = false) = {
      val m = mockUpdateWriteResult(fails)

      when(
        collection.update(eqTo(selector), eqTo(obj), any(), anyBoolean, anyBoolean)(any(), any(), any[ExecutionContext])
      ) thenReturn Future.successful(m)

      this
    }

    def setupAnyUpdate(fails: Boolean = false) = {
      val m = mockUpdateWriteResult(fails)
      when(
        collection.update(any(), any(), any(), anyBoolean, anyBoolean)(any(), any(), any[ExecutionContext])
      ) thenReturn Future.successful(m)
    }

    private def mockUpdateWriteResult(fails: Boolean = false) = {
      val m = mock[UpdateWriteResult]
      when(m.ok).thenReturn(!fails)
      m
    }

    private def mockWriteResult(fails: Boolean = false) = {
      val m = mock[WriteResult]
      when(m.ok).thenReturn(!fails)
      m
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
