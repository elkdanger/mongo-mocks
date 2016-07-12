import org.elkdanger.testing.MongoMocks
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}

abstract class SpecBase extends FunSpec with MongoMocks with ShouldMatchers {

  protected def await[T](result: Future[T]) = Await.result(result, 5 seconds)

}
