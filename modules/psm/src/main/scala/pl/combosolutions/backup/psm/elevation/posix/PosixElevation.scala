package pl.combosolutions.backup.psm.elevation.posix

import pl.combosolutions.backup.psm.OperatingSystem
import pl.combosolutions.backup.psm.elevation.CommonElevation

object SudoElevation extends CommonElevation {

  override lazy val elevationAvailable: Boolean = OperatingSystem.current.isPosix

  override val elevationCMD: String = "sudo"
}
