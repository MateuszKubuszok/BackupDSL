package pl.combosolutions.backup.psm.filesystem.posix

import java.nio.file.Path
import pl.combosolutions.backup.psm.OperatingSystem
import pl.combosolutions.backup.psm.elevation.{ElevateIfNeeded, ElevationMode}
import ElevateIfNeeded._
import pl.combosolutions.backup.psm.filesystem.FileType.FileType
import pl.combosolutions.backup.psm.operations.{Cleaner, PlatformSpecificFileSystem}
import pl.combosolutions.backup.psm.programs.posix.{FileInfo, PosixPrograms}
import PosixPrograms._

object PosixFileSystem extends PlatformSpecificFileSystem {
  override lazy val fileSystemAvailable = OperatingSystem.current.isPosix

  override lazy val fileIsFile = "(.*): directory".r
  override lazy val fileIsDirectory = "(.*): directory".r
  override lazy val fileIsSymlinkPattern = "(.*): symbolic link to .*'".r

  override def getFileType(forPath: Path)(implicit withElevation: ElevationMode, cleaner: Cleaner) =
    FileInfo(forPath.toAbsolutePath.toString).handleElevation.digest[FileType]
}
