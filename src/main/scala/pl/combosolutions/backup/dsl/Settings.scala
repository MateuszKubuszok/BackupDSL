package pl.combosolutions.backup.dsl

import org.apache.commons.lang3.SystemUtils

import java.nio.file.{CopyOption, Path}
import java.nio.file.StandardCopyOption._

case class Settings(
  var backupDir: Path,
  var copyOptions: Array[CopyOption]
)

object Settings extends Settings(
  backupDir = java.nio.file.Paths.get(SystemUtils.getUserHome.getPath, "backup_dsl"),
  copyOptions = Array(REPLACE_EXISTING, COPY_ATTRIBUTES)
)
