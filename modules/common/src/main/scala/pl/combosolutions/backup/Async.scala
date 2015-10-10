package pl.combosolutions.backup

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ ExecutionContext, Future }

object Async {

  def apply[Result](action: => Option[Result])(implicit executor: ExecutionContext): Async[Result] = Future { action }

  def some[Result](value: Result): Async[Result] = Future successful Some(value)

  def none[Result]: Async[Result] = Future successful None

  def successful[Result](value: Option[Result]): Async[Result] = Future successful value

  def failed[Result](exception: Throwable): Async[Result] = Future failed exception

  def completeSequence[Result, M[X] <: TraversableOnce[X]](in: M[Async[Result]])(implicit cbf: CanBuildFrom[M[Future[Option[Result]]], Option[Result], M[Option[Result]]],
    cbf2: CanBuildFrom[M[Option[Result]], Result, M[Result]], executor: ExecutionContext): Async[M[Result]] =
    Future sequence in map { resultOpts =>
      if (resultOpts exists (_.isEmpty)) None
      else
        resultOpts.foldLeft(Option(cbf2(resultOpts))) {
          (or, oa) => for (r <- or; a <- oa) yield r += a
        } map (_.result())
    }

  def incompleteSequence[Result, M[X] <: TraversableOnce[X]](in: M[Async[Result]])(implicit cbf: CanBuildFrom[M[Future[Option[Result]]], Option[Result], M[Option[Result]]],
    cbf2: CanBuildFrom[M[Option[Result]], Result, M[Result]], executor: ExecutionContext): Async[M[Result]] =
    Future sequence in map { resultOpts =>
      resultOpts.foldLeft(Option(cbf2(resultOpts))) {
        (or, oa) => for (r <- or; a <- oa) yield r += a
      } map (_.result())
    }

  def flatMap[Result, NewResult](result: Async[Result], function: Result => Async[NewResult])(implicit executor: ExecutionContext): Async[NewResult] =
    result flatMap (_ map (function(_)) getOrElse none)

  def map[Result, NewResult](result: Async[Result], function: Result => NewResult)(implicit executor: ExecutionContext): Async[NewResult] =
    result map (_ map function)
}
