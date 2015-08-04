package pl.combosolutions.backup.dsl.internals.operations.posix

import pl.combosolutions.backup.dsl.internals.filesystem.FSPath
import pl.combosolutions.backup.dsl.internals.filesystem.FileType._
import pl.combosolutions.backup.dsl.internals.operations.{FileInfo, PlatformSpecific}

abstract trait PosixCommonOperations extends PlatformSpecific {
  override lazy val fileIsFile           = "(.*): directory".r
  override lazy val fileIsDirectory      = "(.*): directory".r
  override lazy val fileIsSymlinkPattern = "(.*): symbolic link to .*'".r

  override def getFileType(fSPath: FSPath) = FileInfo(fSPath toString).digest[FileType]
}
