package pl.combosolutions.backup.psm.filesystem.posix

import java.nio.file.StandardCopyOption._

import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.filesystem.CommonFilesServiceComponent
import pl.combosolutions.backup.psm.systems.{ OperatingSystemComponent, OperatingSystemComponentImpl }

trait PosixFilesServiceComponent extends CommonFilesServiceComponent {
  self: PosixFilesServiceComponent with OperatingSystemComponent =>

  override lazy val available = operatingSystem.isPosix

  override lazy val priority = if (available) OnlyAllowed else NotAllowed

  protected val withCopyOptions = List(COPY_ATTRIBUTES, REPLACE_EXISTING)

  protected val withMoveOptions = List(REPLACE_EXISTING)
}

object PosixFilesServiceComponent
  extends PosixFilesServiceComponent
  with OperatingSystemComponentImpl
