package org.elkdanger.testing

import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import reactivemongo.api.indexes.CollectionIndexesManager
import reactivemongo.api.{CollectionProducer, DefaultDB, FailoverStrategy}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

trait MongoMockCollections extends MockitoSugar with MongoMockUpdates {

  def mockCollection(name: Option[String] = None)(implicit db: DefaultDB, ec: ExecutionContext): JSONCollection = {

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
