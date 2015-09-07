package pl.combosolutions.backup.dsl.internals.elevation.posix.linux

import pl.combosolutions.backup.dsl.internals.elevation.CommonElevation

object GKSudoElevation extends CommonElevation {

  override val elevationCMD = "gksudo"

  override val elevationArgs = List("-m", "BackupDSL elevation runner", "--")
}

object KDESudoElevation extends CommonElevation {

  override val elevationCMD = "kdesudo"

  override val elevationArgs = List("--")
}

