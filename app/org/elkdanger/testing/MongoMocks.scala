package org.elkdanger.testing

import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.DefaultDB

import scala.concurrent.Future

trait MongoMocks extends MockitoSugar
  with MongoMockFinds
  with MongoMockInserts
  with MongoMockUpdates
  with MongoMockCollections {

  implicit val mockMongoApi = mock[ReactiveMongoApi]
  implicit val mockMongoDb = mock[DefaultDB]

  when(mockMongoApi.database).thenReturn(Future.successful(mockMongoDb))

}
