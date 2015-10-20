package pl.combosolutions.backup.psm.elevation.posix.linux

import pl.combosolutions.backup.psm.elevation
import elevation.{ ElevationService, ElevationFacadeComponentImpl, ElevationFacadeComponent, ElevationServiceComponent }
import elevation.posix.CommonElevationServiceComponent
import pl.combosolutions.backup.psm.systems._

trait GKSudoElevationServiceComponent extends CommonElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent with AvailableCommandsComponent =>

  override def elevationService: ElevationService = GKSudoElevationService

  trait GKSudoElevationService extends CommonElevationService {

    override lazy val elevationAvailable = availableCommands.gkSudo

    override val elevationCMD = "gksudo"

    override val elevationArgs = List("-m", "BackupDSL elevation runner", "--")

    override val desktopSessionsPreferringThis: Set[String] = Set("gnome")
  }

  object GKSudoElevationService extends GKSudoElevationService
}

object GKSudoElevationServiceComponent
  extends GKSudoElevationServiceComponent
  with ElevationFacadeComponentImpl
  with OperatingSystemComponentImpl
  with AvailableCommandsComponentImpl

trait KDESudoElevationServiceComponent extends CommonElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent with AvailableCommandsComponent =>

  override def elevationService: ElevationService = KDESudoElevationService

  trait KDESudoElevationService extends CommonElevationService {

    override lazy val elevationAvailable = availableCommands.kdeSudo

    override val elevationCMD = "kdesudo"

    override val elevationArgs = List("-m", "BackupDSL elevation runner", "--")

    override val desktopSessionsPreferringThis: Set[String] = Set("kde")
  }

  object KDESudoElevationService extends KDESudoElevationService
}

object KDESudoElevationServiceComponent
  extends KDESudoElevationServiceComponent
  with ElevationFacadeComponentImpl
  with OperatingSystemComponentImpl
  with AvailableCommandsComponentImpl
