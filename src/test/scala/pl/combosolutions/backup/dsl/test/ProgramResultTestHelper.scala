package pl.combosolutions.backup.dsl.test

import org.specs2.matcher._
import pl.combosolutions.backup.dsl.internals.operations.Program._
import pl.combosolutions.backup.dsl.internals.operations.Result

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait ProgramResultTestHelper {

  def beCorrectProgramResult[ResultType]: Matcher[AsyncResult[Result[ResultType]]] = new BeCorrectProgramResult[ResultType]
  class BeCorrectProgramResult[ResultType] extends Matcher[AsyncResult[Result[ResultType]]] {
    override def apply[S <: AsyncResult[Result[ResultType]]](t: Expectable[S]): MatchResult[S] =
      Await.result(t.value, Duration.Inf) match {
        case Some(programResult) => MatchSuccess[S]("Correct program result", "", t)
        case _                   => MatchFailure[S]("", "Program execution failed", t)
      }
  }
}
