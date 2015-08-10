package pl.combosolutions.backup.dsl.internals.operations.posix

import pl.combosolutions.backup.dsl.internals.OperatingSystem
import pl.combosolutions.backup.dsl.internals.elevation.{ElevationFacade, DirectElevatorProgram}
import pl.combosolutions.backup.dsl.internals.operations.{CommonElevation, Cleaner, Program, PlatformSpecificElevation}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SudoElevation extends CommonElevation {

  override lazy val elevationAvailable: Boolean = OperatingSystem.current.isPosix

  override val elevationCMD: String = "sudo"
}
