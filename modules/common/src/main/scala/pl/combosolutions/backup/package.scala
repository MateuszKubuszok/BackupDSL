package pl.combosolutions

import scala.concurrent.{ ExecutionContext, Future }

package object backup {

  type Async[U] = Future[Option[U]]

  implicit class AsyncTransformer[ResultT](result: Async[ResultT]) {

    def asAsync: AsyncTransformer[ResultT] = this

    // format: OFF
    def flatMap[NewResult](function: ResultT => Async[NewResult])
                          (implicit executor: ExecutionContext): Async[NewResult] = Async.flatMap(result, function)

    def map[NewResult](function: ResultT => NewResult)
                      (implicit executor: ExecutionContext): Async[NewResult] = Async.map(result, function)
    // format: ON
  }
}
