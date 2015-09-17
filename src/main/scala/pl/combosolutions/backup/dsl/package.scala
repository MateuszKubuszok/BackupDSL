package pl.combosolutions.backup

import pl.combosolutions.backup.dsl.AsyncResult.AsyncResultTransformer

import scala.concurrent.Future

package object dsl {

  type AsyncResult[U] = Future[Option[U]]

  implicit def wrapAsyncResultForMapping[Result](result: AsyncResult[Result]) = new AsyncResultTransformer(result)
}
