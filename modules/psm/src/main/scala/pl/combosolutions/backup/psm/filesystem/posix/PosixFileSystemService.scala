package pl.combosolutions.backup.psm.filesystem.posix

import java.nio.file.Path
import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.elevation.{ ElevateIfNeeded, ElevationMode }
import ElevateIfNeeded._
import pl.combosolutions.backup.psm.filesystem.FileSystemService
import pl.combosolutions.backup.psm.filesystem.FileType.FileType
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.posix.{ FileInfo, PosixPrograms }
import PosixPrograms._
import pl.combosolutions.backup.psm.systems.OperatingSystemComponent

object PosixFileSystemService extends FileSystemService with ComponentsHelper {
  this: FileSystemService with OperatingSystemComponent =>

  override lazy val fileSystemAvailable = operatingSystem.isPosix

  override lazy val fileIsFile = "(.*): directory".r
  override lazy val fileIsDirectory = "(.*): directory".r
  override lazy val fileIsSymlinkPattern = "(.*): symbolic link to .*'".r

  override def getFileType(forPath: Path)(implicit withElevation: ElevationMode, cleaner: Cleaner) =
    FileInfo(forPath.toAbsolutePath.toString).handleElevation.digest[FileType]
}
