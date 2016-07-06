package org.elkdanger.testing

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{ eq => eqTo, _ }
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import reactivemongo.api.commands._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

trait MongoMockInserts extends MockitoSugar {

  def setupInsertOn[T](collection: JSONCollection, obj: T, fails: Boolean = false) = {
    val m = mockWriteResult(fails)
    when(collection.insert(eqTo(obj), any())(any(), any()))
      .thenReturn(Future.successful(m))
  }

  def setupAnyInsertOn(collection: JSONCollection, fails: Boolean = false) = {
    val m = mockWriteResult(fails)
    when(collection.insert(any(), any())(any(), any()))
      .thenReturn(Future.successful(m))
  }

  def verifyAnyInsertOn(collection: JSONCollection) = {
    verify(collection).insert(any, any())(any(), any())
  }

  def verifyInsertWith[T](collection: JSONCollection, obj: T) = {
    verify(collection).insert(eqTo(obj), any())(any(), any())
  }

  def verifyInsertWith[T](collection: JSONCollection, captor: ArgumentCaptor[T]) = {
    verify(collection).insert(captor.capture(), any[WriteConcern])(any(), any[ExecutionContext])
  }

  private def mockWriteResult(fails: Boolean = false) = {
    val m = mock[WriteResult]
    when(m.ok).thenReturn(!fails)
    m
  }

}
