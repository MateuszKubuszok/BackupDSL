package pl.combosolutions.backup.psm.elevation.posix

import pl.combosolutions.backup.Cleaner
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.commands.Command
import pl.combosolutions.backup.psm.elevation._
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.psm.systems._

import CommonElevationServiceComponent._

object CommonElevationServiceComponent {

  val DesktopSessionEnv = "DESKTOP_SESSION"
  val CurrentDesktopSession = System getenv DesktopSessionEnv
}

trait CommonElevationServiceComponent extends ElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent =>

  val currentDesktopSession = CurrentDesktopSession

  trait CommonElevationService extends ElevationService {

    val desktopSessionsPreferringThis: Set[String]

    override lazy val elevationPriority: ImplementationPriority = {
      if (elevationAvailable) {
        currentDesktopSession match {
          case "" => OnlyAllowed
          case _ if (desktopSessionsPreferringThis contains currentDesktopSession) => Preferred
          case _ => Allowed
        }
      } else NotAllowed
    }

    override def elevateDirect[T <: Program[T]](program: Program[T]): Program[T] =
      DirectElevatorProgram[T](program, this)

    override def elevateRemote[T <: Command[T]](command: Command[T], cleaner: Cleaner): Command[T] =
      RemoteElevatorCommand[T](command, elevationFacadeFor(cleaner))

    override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T] =
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

    override val desktopSessionsPreferringThis: Set[String] = Set("")
  }

  object SudoElevationService extends SudoElevationService
}

object SudoElevationServiceComponent
  extends SudoElevationServiceComponent
  with ElevationFacadeComponentImpl
  with OperatingSystemComponentImpl
