package pl.combosolutions.backup.dsl

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ ExecutionContext, Future }

object AsyncResult {

  def apply[Result](value: Result): AsyncResult[Result] = Future successful Some(value)

  def apply[Result](value: Option[Result]): AsyncResult[Result] = Future successful value

  def apply[Result](action: () => Result)(implicit executor: ExecutionContext): AsyncResult[Result] = Future { Some(action()) }

  def failed[Result](exception: Throwable): AsyncResult[Result] = Future failed exception

  def completeSequence[Result, M[X] <: TraversableOnce[X]](in: M[AsyncResult[Result]])(implicit cbf: CanBuildFrom[M[Future[Option[Result]]], Option[Result], M[Option[Result]]], cbf2: CanBuildFrom[M[Option[Result]], Result, M[Result]], executor: ExecutionContext): AsyncResult[M[Result]] =
    Future sequence in map { resultOpts =>
      if (resultOpts exists (_.isEmpty)) None
      else
        resultOpts.foldLeft(Option(cbf2(resultOpts))) {
          (or, oa) => for (r <- or; a <- oa) yield r += a
        } map (_.result())
    }

  def incompleteSequence[Result, M[X] <: TraversableOnce[X]](in: M[AsyncResult[Result]])(implicit cbf: CanBuildFrom[M[Future[Option[Result]]], Option[Result], M[Option[Result]]], cbf2: CanBuildFrom[M[Option[Result]], Result, M[Result]], executor: ExecutionContext): AsyncResult[M[Result]] =
    Future sequence in map { resultOpts =>
      resultOpts.foldLeft(Option(cbf2(resultOpts))) {
        (or, oa) => for (r <- or; a <- oa) yield r += a
      } map (_.result())
    }

  def flatMap[Result, NewResult](result: AsyncResult[Result], function: Result => AsyncResult[NewResult])(implicit executor: ExecutionContext): AsyncResult[NewResult] =
    result flatMap (_ map (function(_)) getOrElse AsyncResult(scala.None))

  def map[Result, NewResult](result: AsyncResult[Result], function: Result => NewResult)(implicit executor: ExecutionContext): AsyncResult[NewResult] =
    result map (_ map function)
}
