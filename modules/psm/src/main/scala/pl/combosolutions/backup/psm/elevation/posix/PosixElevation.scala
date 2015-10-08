package pl.combosolutions.backup.psm.elevation.posix

import pl.combosolutions.backup.psm.elevation._
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.psm.programs.posix.PosixPrograms._
import pl.combosolutions.backup.psm.programs.posix.WhichProgram
import pl.combosolutions.backup.psm.systems.{ OperatingSystemComponentImpl, OperatingSystemComponent }

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait CommonElevationServiceComponent extends ElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent =>

  trait CommonElevationService extends ElevationService {

    override lazy val elevationAvailable: Boolean =
      Await.result(WhichProgram(elevationCMD).digest[Boolean], Duration.Inf) getOrElse false

    override def elevateDirect[T <: Program[T]](program: Program[T]) =
      DirectElevatorProgram[T](program, this)

    override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
      RemoteElevatorProgram[T](program, elevationFacadeFor(cleaner))
  }
}

trait SudoElevationServiceComponent extends CommonElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent with OperatingSystemComponent =>

  override def elevationService: ElevationService = SudoElevationService

  // TODO: use http://stackoverflow.com/questions/18708087/how-to-execute-bash-command-with-sudo-privileges-in-java
  trait SudoElevationService extends CommonElevationService {

    override lazy val elevationAvailable: Boolean = operatingSystem.isPosix

    override val elevationCMD: String = "sudo"
  }

  object SudoElevationService extends SudoElevationService
}

object SudoElevationServiceComponent
  extends SudoElevationServiceComponent
  with ElevationFacadeComponentImpl
  with OperatingSystemComponentImpl
