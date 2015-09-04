package pl.combosolutions.backup.dsl.internals.elevation.posix

import pl.combosolutions.backup.dsl.internals.OperatingSystem
import pl.combosolutions.backup.dsl.internals.elevation.CommonElevation

object SudoElevation extends CommonElevation {

  override lazy val elevationAvailable: Boolean = OperatingSystem.current.isPosix

  override val elevationCMD: String = "sudo"
}
