package pl.combosolutions.backup

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object Async extends Reporting {

  def apply[Result](action: => Option[Result])(implicit executor: ExecutionContext): Async[Result] =
    Future { action } recoverWith notifyError

  def some[Result](value: Result): Async[Result] = Future successful Some(value)

  def none[Result]: Async[Result] = Future successful None

  def successful[Result](value: Option[Result]): Async[Result] = Future successful value

  def failed[Result](exception: Throwable): Async[Result] = Future failed exception

  // format: OFF
  def completeSequence[Result, M[X] <: TraversableOnce[X]]
      (in: M[Async[Result]])
      (implicit cbf: CanBuildFrom[M[Future[Option[Result]]], Option[Result], M[Option[Result]]],
       cbf2: CanBuildFrom[M[Option[Result]], Result, M[Result]], executor: ExecutionContext): Async[M[Result]] = for {
      sequence <- Future sequence in
      isComplete = sequence forall (_.isDefined)
      value <- if (isComplete) incompleteSequence(in) else none
    } yield value

  def incompleteSequence[Result, M[X] <: TraversableOnce[X]]
      (in: M[Async[Result]])
      (implicit cbf: CanBuildFrom[M[Future[Option[Result]]], Option[Result], M[Option[Result]]],
       cbf2: CanBuildFrom[M[Option[Result]], Result, M[Result]], executor: ExecutionContext): Async[M[Result]] = (for {
     resultsOpts <- Future sequence in
     sequenceBuilderInit = Option(cbf2(resultsOpts))
     sequenceBuilderOpt = resultsOpts.foldLeft(sequenceBuilderInit)(seqOptFolder[Result, M])
   } yield sequenceBuilderOpt map (_.result)) recoverWith notifyError

  def flatMap[Result, NewResult](resultA: Async[Result], function: Result => Async[NewResult])
                                (implicit executor: ExecutionContext): Async[NewResult] = (for {
    result <- optionT(resultA)
    mapped <- optionT(function(result))
  } yield mapped).run recoverWith notifyError

  def map[Result, NewResult](resultA: Async[Result], function: Result => NewResult)
                            (implicit executor: ExecutionContext): Async[NewResult] = (for {
    result <- optionT(resultA)
  } yield function(result)).run recoverWith notifyError

  private def notifyError[Result]: PartialFunction[Throwable, Async[Result]] = {
    case ex: Throwable =>
      reporter error ("Failed to execute Async Result properly", ex)
      Async.failed[Result](ex)
  }

  private def seqOptFolder[Result, M[X] <: TraversableOnce[X]]
      (builderOpt: Option[mutable.Builder[Result, M[Result]]], appendedOpt: Option[Result]) = for {
    builder <- builderOpt
    appended <- appendedOpt
  } yield builder += appended
  // format: ON
}
