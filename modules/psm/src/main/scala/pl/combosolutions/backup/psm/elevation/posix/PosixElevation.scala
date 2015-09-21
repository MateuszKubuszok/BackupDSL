package pl.combosolutions.backup.psm.elevation.posix

import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.elevation.CommonElevationService
import pl.combosolutions.backup.psm.systems.OperatingSystemComponent

object SudoElevationService extends CommonElevationService with ComponentsHelper {
  this: CommonElevationService with OperatingSystemComponent =>

  override lazy val elevationAvailable: Boolean = operatingSystem.isPosix

  override val elevationCMD: String = "sudo"
}
