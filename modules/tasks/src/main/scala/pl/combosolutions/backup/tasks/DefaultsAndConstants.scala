package pl.combosolutions.backup.tasks

import java.nio.file.CopyOption
import java.nio.file.StandardCopyOption._

import org.apache.commons.lang3.SystemUtils

object DefaultsAndConstants {

  val BackupDirName = "backup_dsl"
  val BackupDirPath = java.nio.file.Paths.get(SystemUtils.getUserHome.getPath, BackupDirName)
  val CopyOptions = Array[CopyOption](REPLACE_EXISTING, COPY_ATTRIBUTES)
}
