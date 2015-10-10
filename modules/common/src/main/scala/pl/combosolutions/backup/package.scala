package pl.combosolutions

import scala.concurrent.{ ExecutionContext, Future }

package object backup {

  type Async[U] = Future[Option[U]]

  implicit def wrapAsyncForMapping[Result](result: Async[Result]) = new AsyncTransformer(result)

  implicit class AsyncTransformer[Result](result: Async[Result]) {

    def asAsync = this

    def flatMap[NewResult](function: Result => Async[NewResult])(implicit executor: ExecutionContext) = Async.flatMap(result, function)

    def map[NewResult](function: Result => NewResult)(implicit executor: ExecutionContext) = Async.map(result, function)
  }
}
