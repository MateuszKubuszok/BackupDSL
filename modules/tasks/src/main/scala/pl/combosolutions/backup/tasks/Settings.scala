package pl.combosolutions.backup.tasks

import java.nio.file.{ CopyOption, Path }

import pl.combosolutions.backup.tasks.DefaultsAndConstants.{ BackupDirPath, CopyOptions }

case class Settings(
  var backupDir:     Path,
  var copyOptions:   Array[CopyOption],
  var withElevation: Boolean
)

object Settings extends Settings(
  backupDir     = BackupDirPath,
  copyOptions   = CopyOptions,
  withElevation = false
)
