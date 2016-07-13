package org.elkdanger.testing

import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsObject
import reactivemongo.api.commands._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{Future, ExecutionContext}

trait MongoMockUpdates extends MockitoSugar {

  implicit class UpdateMethods(collection: JSONCollection) {

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
}
