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
}
