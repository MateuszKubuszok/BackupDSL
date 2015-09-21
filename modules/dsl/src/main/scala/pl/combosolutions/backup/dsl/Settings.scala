package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.psm.DefaultsAndConstants.{BackupDirPath, CopyOptions}

import java.nio.file.{ CopyOption, Path }

case class Settings(
  var backupDir: Path,
  var copyOptions: Array[CopyOption],
  var withElevation: Boolean)

object Settings extends Settings(
  backupDir = BackupDirPath,
  copyOptions = CopyOptions,
  withElevation = false
)
