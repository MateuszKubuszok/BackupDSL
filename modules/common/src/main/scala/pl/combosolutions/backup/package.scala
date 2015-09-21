package pl.combosolutions

import scala.concurrent.{ExecutionContext, Future}

package object backup {

  type AsyncResult[U] = Future[Option[U]]

  implicit def wrapAsyncResultForMapping[Result](result: AsyncResult[Result]) = new AsyncResultTransformer(result)

  implicit class AsyncResultTransformer[Result](result: AsyncResult[Result]) {

    def asAsync = this

    def flatMap[NewResult](function: Result => AsyncResult[NewResult])(implicit executor: ExecutionContext) = AsyncResult.flatMap(result, function)

    def map[NewResult](function: Result => NewResult)(implicit executor: ExecutionContext) = AsyncResult.map(result, function)
  }
}
