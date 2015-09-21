package pl.combosolutions.backup.psm.elevation.posix.linux

import pl.combosolutions.backup.psm.elevation.CommonElevation

object GKSudoElevation extends CommonElevation {

  override val elevationCMD = "gksudo"

  override val elevationArgs = List("-m", "BackupDSL elevation runner", "--")
}

object KDESudoElevation extends CommonElevation {

  override val elevationCMD = "kdesudo"

  override val elevationArgs = List("--")
}

