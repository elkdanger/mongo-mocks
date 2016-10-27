package org.elkdanger.testing

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.api.commands._
import reactivemongo.play.json.collection.{JSONCollection, JSONQueryBuilder}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.Manifest

trait MongoMockExtensions extends MockitoSugar {

  private object QueryBuilder extends MockitoSugar {

    def apply(collection: JSONCollection, filter: Option[JsObject] = None) = {
      val queryBuilder = mock[JSONQueryBuilder]
      val f = if (filter.isEmpty) any() else eqTo(filter.get)

      when(
        collection.find(f)(any())
      ) thenReturn queryBuilder

      queryBuilder
    }
  }

  implicit class FindSetups(collection: JSONCollection) {

    def setupFind[T](returns: Traversable[T])(implicit manifest: Manifest[T]) = {
      setupCursorWithCollect(returns, QueryBuilder(collection))
    }

    def setupFind[T](filter: JsObject, returns: Traversable[T])(implicit manifest: Manifest[T]) = {
      setupCursorWithCollect(returns, QueryBuilder(collection, Some(filter)))
    }

    def setupFind[T](returns: Option[T])(implicit manifest: Manifest[T]) = {
      setupOne(returns, QueryBuilder(collection))
    }

    def setupFind[T](filter: JsObject, returns: Option[T])(implicit manifest: Manifest[T]) = {
      setupOne(returns, QueryBuilder(collection, Some(filter)))
    }

    private def setupCursorWithCollect[T](l: Traversable[T], queryBuilder: JSONQueryBuilder)(implicit manifest: Manifest[T]): Unit = {

      val cursor = mock[Cursor[T]]

      when(
        queryBuilder.cursor[T](any(), any())(any(), any(), any())
      ) thenAnswer new Answer[Cursor[T]] {
        def answer(i: InvocationOnMock) = cursor
      }

      when(
        cursor.collect[Traversable](anyInt, anyBoolean)(any[CanBuildFrom[Traversable[_], T, Traversable[T]]], any[ExecutionContext])
      ) thenReturn Future.successful(l)
    }

    private def setupOne[T](returns: Option[T] = None, queryBuilder: JSONQueryBuilder)(implicit manifest: Manifest[T]): Unit = {
      when(
        queryBuilder.one[T](any(), any)
      ) thenReturn Future.successful(returns)
    }
  }

  implicit class InsertSetups(collection: JSONCollection) {

    def setupInsert [T](obj: T, fails: Boolean = false) = {
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

    private def mockWriteResult(fails: Boolean = false) = {
      val m = mock[WriteResult]
      when(m.ok).thenReturn(!fails)
      m
    }
  }

  implicit class UpdateSetups(collection: JSONCollection) {

    def verifyUpdate[T](selectorFunc: (JsObject) => Unit = null, objectFunc: (JsObject) => Unit = null) = {
      val filterCaptor = ArgumentCaptor.forClass(classOf[JsObject])
      val updaterCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      verify(collection).update(filterCaptor.capture(), updaterCaptor.capture(), any[WriteConcern], anyBoolean(), anyBoolean())(any(), any(), any[ExecutionContext])

      if (selectorFunc != null)
        selectorFunc(filterCaptor.getValue)

      if (objectFunc != null)
        objectFunc(updaterCaptor.getValue)
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

  }

  implicit class RemoveSetups(collection: JSONCollection) {
    def setupRemove[S](selector: S, fails: Boolean = false) = {
      val m = mockWriteResult(fails)
      when(collection.remove(eqTo(selector), any(), any())(any(), any())) thenReturn Future.successful(m)
    }

    def setupAnyRemove(fails: Boolean = false) = {
      val m = mockWriteResult(fails)
      when(collection.remove(any(), any(), any())(any(), any())) thenReturn Future.successful(m)
    }

    def verifyRemove[S](selector: S) = {
      verify(collection).remove(eqTo(selector), any(), any())(any(), any())
    }

    def verifyRemove[T](captor: ArgumentCaptor[T]) = {
      verify(collection).remove(captor.capture(), any(), any())(any(), any())
    }

    def verifyAnyRemove = {
      verify(collection).remove(any(), any[WriteConcern], any())(any(), any())
    }

    private def mockWriteResult(fails: Boolean) = {
      val m = mock[WriteResult]
      when(m.ok) thenReturn !fails
      m
    }
  }

}
