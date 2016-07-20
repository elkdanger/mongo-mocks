import org.mockito.ArgumentCaptor
import play.api.libs.json.Json
import play.modules.reactivemongo.json._
import reactivemongo.api.{CollectionProducer, FailoverStrategy}
import reactivemongo.play.json.collection.JSONCollection

class ApiSpec extends SpecBase {

  case class TestObject(id: Int)

  object TestObject {
    implicit val formats = Json.format[TestObject]
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  describe("The mock collection api") {

    it ("should be able to create a mock collection") {
      val collection = MockCollection()
      collection should not be Nil
    }

    it ("should be able to create a mock collection with a specific name") {
      val collection = MockCollection(Some("users"))
      collection should not be Nil
      collection ~> Some(1)

      intercept[NullPointerException] {
        val c = mockMongoDb.collection[JSONCollection]("random_collection")
        val result = await(c.find(Json.obj()).one[Int])

        result.get should be(1)
      }
    }

    describe ("the find methods") {

      val collection = MockCollection()

      it ("should be able to mock a find for any object") {

        val obj = mock[TestObject]

        collection.setupFind(Some(obj))

        val result = await(collection.find(Json.obj()).one[TestObject])

        result should be(Some(obj))
      }

      it ("should be able to mock a find for a list of objects") {

        val list = List(mock[TestObject], mock[TestObject])

        collection.setupFind(list)

        val result = await(collection.find(Json.obj()).cursor[TestObject]().collect[List]())

        result should be(list)
      }

      it ("should be able to mock a find for an object, with a filter") {

        val obj = mock[TestObject]

        collection.setupFind(Json.obj("someProp" -> 12), Some(obj))

        val result = await(collection.find(Json.obj("someProp" -> 12)).one[TestObject])

        result should be(Some(obj))
      }
      
      it ("should be able to mock a find for a list, with a filter") {
        val list = List(mock[TestObject], mock[TestObject])

        collection.setupFind(Json.obj("someProp" -> 12), list)

        val result = await(collection.find(Json.obj("someProp" -> 12)).cursor[TestObject]().collect[List]())

        result should be(list)
      }
    }
  }

  describe("The mock fluent API") {

    it ("should create a mock collection") {
      val collection: JSONCollection = MockCollection()

      collection should not be Nil
    }

    it ("should create a mock for any object") {
      val obj = mock[TestObject]
      val collection = MockCollection()

      collection ~> Some(obj)

      val result = await(collection.find(Json.obj()).one[TestObject])

      result.isDefined should be(true)
      result.get should be(obj)
    }

    it ("should create a mock for a list of objects") {
      val list = List(mock[TestObject], mock[TestObject])
      val collection = MockCollection()

      collection ~> list

      val result = await(collection.find(Json.obj()).cursor[TestObject]().collect[List]())

      result should be(list)
    }

    it ("should mock a find for an object, with a filter") {
      val obj = mock[TestObject]
      val collection = MockCollection()

      collection ? ("id" -> 4) ~> Some(obj)

      val result = await(collection.find(Json.obj("id" -> 4)).one[TestObject])

      result.isDefined should be(true)
      result.get should be(obj)
    }

    it ("should mock a find for a list, with a filter") {
      val list = List(mock[TestObject], mock[TestObject])
      val collection = MockCollection()

      collection ? ("someProp" -> true) ~> list

      val result = await(collection.find(Json.obj("someProp" -> true)).cursor[TestObject]().collect[List]())

      result should be (list)
    }
  }

  describe("The insert methods") {

    it ("should setup and verify an insert method") {
      val obj = mock[TestObject]
      val collection = MockCollection()

      collection <~ obj

      await(collection.insert(obj))

      collection verifyInsertWith obj
    }

    it ("should setup and verify inserts with an ArgumentCaptor") {
      val obj = mock[TestObject]
      val collection = MockCollection()

      collection <~ obj

      await(collection.insert(obj))

      val captor = ArgumentCaptor.forClass(classOf[TestObject])
      collection verifyInsertWith captor

      captor.getValue should be(obj)
    }

    it ("should setup and verify any insert") {
      val collection = MockCollection()

      collection.setupAnyInsert()

      await(collection.insert(mock[TestObject]))

      collection.verifyAnyInsert
    }
  }

  describe("The update methods") {

    it ("should setup and verify the any update method") {
      val collection = MockCollection()

      collection.setupAnyUpdate()

      await(collection.update(Json.obj("id" -> 1), mock[TestObject]))

      collection.verifyAnyUpdate
    }

    it ("should setup and verify the update method with an object") {

      val selector = Json.obj("id" -> 2)
      val obj = Json.obj("name" -> "John Doe")
      val collection = MockCollection()

      collection.setupUpdate(selector, obj)

      await(collection.update(selector, obj))

      collection.verifyUpdate(
        selectorFunc = { _ should be(selector) },
        updateFunc = { _ should be(obj) })
    }

  }
}
