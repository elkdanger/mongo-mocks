package org.elkdanger.testing

import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsObject
import reactivemongo.api.Cursor
import reactivemongo.play.json.collection.{JSONCollection, JSONQueryBuilder}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.Manifest

trait MongoMockFinds extends MockitoSugar {

  implicit class FindSetups(collection: JSONCollection) {

    def setupFind[T](returns: Traversable[T])(implicit manifest: Manifest[T]) = {
      val queryBuilder = mock[JSONQueryBuilder]
      val cursor = mock[Cursor[T]]

      when(
        collection.find(any[JsObject])(any())
      ) thenReturn queryBuilder

      when(
        queryBuilder.cursor[T](any(), any())(any(), any[ExecutionContext], any())
      ) thenAnswer new Answer[Cursor[T]] {
        def answer(i: InvocationOnMock) = cursor
      }

      when(
        cursor.collect[Traversable](anyInt, anyBoolean)(any[CanBuildFrom[Traversable[_], T, Traversable[T]]], any[ExecutionContext])
      ) thenReturn Future.successful(returns)
    }

    def setupFind[T](returns: Option[T])(implicit manifest: Manifest[T]) = {

      val queryBuilder = mock[JSONQueryBuilder]

      when(
        collection.find(any[JsObject])(any())
      ) thenReturn queryBuilder

      when(
        queryBuilder.one[T](any(), any)
      ) thenReturn Future.successful(returns)

    }

    def setupFind[T](filter: Any, returns: Option[T])(implicit manifest: Manifest[T]) = {

      val queryBuilder = mock[JSONQueryBuilder]

      when(
        collection.find(eqTo(filter))(any())
      ) thenReturn queryBuilder

      when(
        queryBuilder.one[T](any(), any)
      ) thenReturn Future.successful(returns)

    }

    def setupFind[T](filter: Any, returns: Traversable[T])(implicit manifest: Manifest[T]) = {

      val queryBuilder = mock[JSONQueryBuilder]
      val cursor = mock[Cursor[T]]

      when(
        collection.find(eqTo(filter))(any())
      ) thenReturn queryBuilder

      when(
        queryBuilder.cursor[T](any(), any())(any(), any(), any())
      ) thenAnswer new Answer[Cursor[T]] {
        def answer(i: InvocationOnMock) = cursor
      }

      when(
        cursor.collect[Traversable](anyInt, anyBoolean)(any[CanBuildFrom[Traversable[_], T, Traversable[T]]], any[ExecutionContext])
      ) thenReturn Future.successful(returns)

    }
  }
}
