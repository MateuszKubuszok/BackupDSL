package pl.combosolutions.backup.test

import org.specs2.matcher._
import org.specs2.mutable.Specification
import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.psm.programs.Result

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait ProgramResultTestHelper {
  self: Specification =>

  def beCorrectProgramResult[ResultType]: Matcher[AsyncResult[Result[ResultType]]] = new BeCorrectProgramResult[ResultType]
  class BeCorrectProgramResult[ResultType] extends Matcher[AsyncResult[Result[ResultType]]] {
    override def apply[S <: AsyncResult[Result[ResultType]]](t: Expectable[S]): MatchResult[S] =
      Await.result(t.value, Duration.Inf) match {
        case Some(programResult) => MatchSuccess[S]("Correct program result", "", t)
        case _ => MatchFailure[S]("", "Program execution failed", t)
      }
  }
}
