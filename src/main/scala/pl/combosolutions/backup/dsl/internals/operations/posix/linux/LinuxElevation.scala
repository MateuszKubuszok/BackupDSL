package pl.combosolutions.backup.dsl.internals.operations.posix.linux

import pl.combosolutions.backup.dsl.internals.operations.CommonElevation

object GKSudoElevation extends CommonElevation {

  override val elevationCMD = "gksudo"
}

object KDESudoElevation extends CommonElevation {

  override val elevationCMD = "kdesudo"
}

