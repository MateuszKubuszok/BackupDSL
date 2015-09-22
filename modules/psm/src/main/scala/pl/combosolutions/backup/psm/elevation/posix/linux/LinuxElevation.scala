package pl.combosolutions.backup.psm.elevation.posix.linux

import pl.combosolutions.backup.psm.elevation.posix.CommonElevationService

object GKSudoElevationService extends CommonElevationService {

  override val elevationCMD = "gksudo"

  override val elevationArgs = List("-m", "BackupDSL elevation runner", "--")
}

object KDESudoElevationService extends CommonElevationService {

  override val elevationCMD = "kdesudo"

  override val elevationArgs = List("--")
}

