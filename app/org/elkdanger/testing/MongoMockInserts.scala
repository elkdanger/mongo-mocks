package org.elkdanger.testing

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{ eq => eqTo, _ }
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import reactivemongo.api.commands._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

trait MongoMockInserts extends MockitoSugar {

  implicit class InsertMethods(collection: JSONCollection) {

    def setupInsert[T](obj: T, fails: Boolean = false) = {
      when(collection.insert(eqTo(obj), any())(any(), any()))
        .thenReturn(Future.successful(mockWriteResult(fails)))
    }

    def setupAnyInsert(fails: Boolean = false) = {
      when(collection.insert(any(), any())(any(), any()))
        .thenReturn(Future.successful(mockWriteResult(fails)))
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



}
