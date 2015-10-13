package pl.combosolutions.backup.psm.elevation.posix.linux

import pl.combosolutions.backup.psm.elevation.{ ElevationService, ElevationFacadeComponentImpl, ElevationFacadeComponent, ElevationServiceComponent }
import pl.combosolutions.backup.psm.elevation.posix.CommonElevationServiceComponent

trait GKSudoElevationServiceComponent extends CommonElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent =>

  override def elevationService: ElevationService = GKSudoElevationService

  trait GKSudoElevationService extends CommonElevationService {

    override val elevationCMD = "gksudo"

    override val elevationArgs = List("-m", "BackupDSL elevation runner", "--")
  }

  object GKSudoElevationService extends GKSudoElevationService
}

object GKSudoElevationServiceComponent
  extends GKSudoElevationServiceComponent
  with ElevationFacadeComponentImpl

trait KDESudoElevationServiceComponent extends CommonElevationServiceComponent {
  self: ElevationServiceComponent with ElevationFacadeComponent =>

  override def elevationService: ElevationService = KDESudoElevationService

  trait KDESudoElevationService extends CommonElevationService {

    override val elevationCMD = "gksudo"

    override val elevationArgs = List("-m", "BackupDSL elevation runner", "--")
  }

  object KDESudoElevationService extends KDESudoElevationService
}

object KDESudoElevationServiceComponent
  extends KDESudoElevationServiceComponent
  with ElevationFacadeComponentImpl
