package pl.combosolutions.backup.dsl.internals.operations

import pl.combosolutions.backup.dsl.internals.elevation.{RemoteElevatorProgram, DirectElevatorProgram, ElevationFacade}
import pl.combosolutions.backup.dsl.internals.operations.posix.PosixPrograms._
import pl.combosolutions.backup.dsl.internals.operations.posix.WhichProgram

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait CommonElevation extends PlatformSpecificElevation {

  override lazy val elevationAvailable: Boolean =
    Await.result(WhichProgram(elevationCMD).digest[Boolean], Duration.Inf) getOrElse false

  override def elevateDirect[T <: Program[T]](program: Program[T]) =
    DirectElevatorProgram[T](program)

  override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner) = {
    RemoteElevatorProgram[T](program, ElevationFacade getFor cleaner)
  }
}
