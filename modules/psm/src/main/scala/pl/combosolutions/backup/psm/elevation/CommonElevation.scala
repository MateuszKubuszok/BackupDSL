package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.psm.operations.{ Cleaner, PlatformSpecificElevation }
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.psm.programs.posix.{ PosixPrograms, WhichProgram }
import PosixPrograms._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait CommonElevation extends PlatformSpecificElevation {

  override lazy val elevationAvailable: Boolean =
    Await.result(WhichProgram(elevationCMD).digest[Boolean], Duration.Inf) getOrElse false

  override def elevateDirect[T <: Program[T]](program: Program[T]) =
    DirectElevatorProgram[T](program)

  override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    RemoteElevatorProgram[T](program, ElevationFacade getFor cleaner)
}
