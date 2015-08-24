package pl.combosolutions.backup.dsl.internals.elevation

import pl.combosolutions.backup.dsl.internals.operations.{PlatformSpecific, Program}
import sun.reflect.generics.reflectiveObjects.NotImplementedException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import scalaz.OptionT._
import scalaz.std.scalaFuture._

case class DirectElevatorProgram[T <: Program[T]](
  program: Program[T]
) extends Program[T](
  PlatformSpecific.current.elevationCMD,
  PlatformSpecific.current.elevationArgs ++ (program.name :: program.arguments)
)

case class RemoteElevatorProgram[T <: Program[T]](
  program: Program[T],
  elevationFacade: ElevationFacade
) extends Program[T](
  program.name,
  program.arguments
) {

  override def run      =  (for {
    result <- optionT[Future](elevationFacade runRemotely program.asGeneric)
  } yield result.asSpecific[T]).run

  override def run2Kill = throw new NotImplementedException
}
