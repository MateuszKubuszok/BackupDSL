package pl.combosolutions.backup.psm.programs

import org.specs2.matcher._
import org.specs2.mutable.Specification
import pl.combosolutions.backup.{ Async, Result }

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait ProgramResultTestHelper {
  self: Specification =>

  def beCorrectProgramResult[ResultType]: Matcher[Async[Result[ResultType]]] = new BeCorrectProgramResult[ResultType]
  class BeCorrectProgramResult[ResultType] extends Matcher[Async[Result[ResultType]]] {
    override def apply[S <: Async[Result[ResultType]]](t: Expectable[S]): MatchResult[S] =
      Await.result(t.value, Duration.Inf) match {
        case Some(programResult) => MatchSuccess[S]("Correct program result", "", t)
        case _                   => MatchFailure[S]("", "Program execution failed", t)
      }
  }
}
