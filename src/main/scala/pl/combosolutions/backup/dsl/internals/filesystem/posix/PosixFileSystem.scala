package pl.combosolutions.backup.dsl.internals.filesystem.posix

import java.nio.file.Path

import pl.combosolutions.backup.dsl.internals.OperatingSystem
import pl.combosolutions.backup.dsl.internals.filesystem.FileType._
import pl.combosolutions.backup.dsl.internals.operations.PlatformSpecificFileSystem
import pl.combosolutions.backup.dsl.internals.programs.posix.FileInfo
import pl.combosolutions.backup.dsl.internals.programs.posix.PosixPrograms._

object PosixFileSystem extends PlatformSpecificFileSystem {
  override lazy val fileSystemAvailable = OperatingSystem.current.isPosix

  override lazy val fileIsFile           = "(.*): directory".r
  override lazy val fileIsDirectory      = "(.*): directory".r
  override lazy val fileIsSymlinkPattern = "(.*): symbolic link to .*'".r

  override def getFileType(path: Path) = FileInfo(path.toAbsolutePath.toString).digest[FileType]
}
