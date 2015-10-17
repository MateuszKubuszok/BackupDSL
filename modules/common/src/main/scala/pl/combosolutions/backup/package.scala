package pl.combosolutions

import scala.concurrent.{ ExecutionContext, Future }

package object backup {

  type Async[U] = Future[Option[U]]

  implicit class AsyncTransformer[Result](result: Async[Result]) {

    def asAsync: AsyncTransformer[Result] = this

    // format: OFF
    def flatMap[NewResult](function: Result => Async[NewResult])
                          (implicit executor: ExecutionContext): Async[NewResult] = Async.flatMap(result, function)

    def map[NewResult](function: Result => NewResult)
                      (implicit executor: ExecutionContext): Async[NewResult] = Async.map(result, function)
    // format: ON
  }
}
