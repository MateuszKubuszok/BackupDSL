package pl.combosolutions.backup

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object Async extends Reporting {

  def apply[ResultT](action: => Option[ResultT])(implicit executor: ExecutionContext): Async[ResultT] =
    Future { action } recoverWith notifyError

  def some[ResultT](value: ResultT): Async[ResultT] = Future successful Some(value)

  def none[ResultT]: Async[ResultT] = Future successful None

  def successful[ResultT](value: Option[ResultT]): Async[ResultT] = Future successful value

  def failed[ResultT](exception: Throwable): Async[ResultT] = Future failed exception

  // format: OFF
  def completeSequence[ResultT, M[X] <: TraversableOnce[X]]
      (in: M[Async[ResultT]])
      (implicit cbf: CanBuildFrom[M[Future[Option[ResultT]]], Option[ResultT], M[Option[ResultT]]],
       cbf2: CanBuildFrom[M[Option[ResultT]], ResultT, M[ResultT]], executor: ExecutionContext): Async[M[ResultT]] =
    for {
      sequence <- Future sequence in
      isComplete = sequence forall (_.isDefined)
      value <- if (isComplete) incompleteSequence(in) else none
    } yield value

  def incompleteSequence[ResultT, M[X] <: TraversableOnce[X]]
      (in: M[Async[ResultT]])
      (implicit cbf: CanBuildFrom[M[Future[Option[ResultT]]], Option[ResultT], M[Option[ResultT]]],
       cbf2: CanBuildFrom[M[Option[ResultT]], ResultT, M[ResultT]], executor: ExecutionContext): Async[M[ResultT]] =
    (for {
      resultsOpts <- Future sequence in
      sequenceBuilderInit = Option(cbf2(resultsOpts))
      sequenceBuilderOpt = resultsOpts.foldLeft(sequenceBuilderInit)(seqOptFolder[ResultT, M])
    } yield sequenceBuilderOpt map (_.result)) recoverWith notifyError

  def flatMap[ResultT, NewResult](resultA: Async[ResultT], function: ResultT => Async[NewResult])
                                 (implicit executor: ExecutionContext): Async[NewResult] = (for {
    result <- optionT(resultA)
    mapped <- optionT(function(result))
  } yield mapped).run recoverWith notifyError

  def map[ResultT, NewResult](resultA: Async[ResultT], function: ResultT => NewResult)
                            (implicit executor: ExecutionContext): Async[NewResult] = (for {
    result <- optionT(resultA)
  } yield function(result)).run recoverWith notifyError

  private def notifyError[ResultT]: PartialFunction[Throwable, Async[ResultT]] = {
    case ex: Throwable =>
      reporter error ("Failed to execute Async Result properly", ex)
      Async failed ex
  }

  private def seqOptFolder[ResultT, M[X] <: TraversableOnce[X]]
      (builderOpt: Option[mutable.Builder[ResultT, M[ResultT]]], appendedOpt: Option[ResultT]) =
    if (appendedOpt.isEmpty) builderOpt
    else for {
      builder  <- builderOpt
      appended <- appendedOpt
    } yield builder += appended
  // format: ON
}
