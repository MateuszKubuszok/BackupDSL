package pl.combosolutions.backup.dsl.internals

import java.nio.file.{CopyOption, Path}
import java.nio.file.StandardCopyOption._

import org.apache.commons.lang3.SystemUtils

object DefaultsAndConsts {
  val BackupDirName = "backup_dsl"
  val BackupDirPath = java.nio.file.Paths.get(SystemUtils.getUserHome.getPath, BackupDirName)
  val CopyOptions   = Array[CopyOption](REPLACE_EXISTING, COPY_ATTRIBUTES)

  val exceptionNoElevation     = "No elevation found"
  val exceptionNoFileSystem    = "No file system found"
  val exceptionNoRepositories  = "No repositories found"
  val exceptionUnknownFileType = "Unexpected `file` answer"
}