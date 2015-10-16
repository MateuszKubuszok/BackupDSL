package pl.combosolutions.backup.tasks

import java.nio.file.{ CopyOption, Path }

import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.ComponentRegistry
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, NotElevated, ObligatoryElevationMode, RemoteElevation }
import pl.combosolutions.backup.tasks.DefaultsAndConstants.{ BackupDirPath, CopyOptions }

object Settings {

  def apply(settings: Settings): ImmutableSettings = ImmutableSettings(
    cleaner                 = settings.cleaner,
    withElevation           = settings.withElevation,
    withObligatoryElevation = settings.withObligatoryElevation,
    components              = settings.components,
    backupDir               = settings.backupDir,
    copyOptions             = settings.copyOptions
  )
}

trait Settings {

  val cleaner: Cleaner
  val withElevation: ElevationMode
  val withObligatoryElevation: ObligatoryElevationMode
  val components: ComponentRegistry
  val backupDir: Path
  val copyOptions: Array[CopyOption]
}

case class ImmutableSettings(
  override val cleaner:                 Cleaner,
  override val withElevation:           ElevationMode           = NotElevated,
  override val withObligatoryElevation: ObligatoryElevationMode = RemoteElevation,
  override val components:              ComponentRegistry       = ComponentRegistry,
  override val backupDir:               Path                    = BackupDirPath,
  override val copyOptions:             Array[CopyOption]       = CopyOptions
) extends Settings

case class MutableSettings(
    var innerSettings: Settings
) extends Settings {

  override val cleaner: Cleaner = innerSettings.cleaner
  override val withElevation: ElevationMode = innerSettings.withElevation
  override val withObligatoryElevation: ObligatoryElevationMode = innerSettings.withObligatoryElevation
  override val components: ComponentRegistry = innerSettings.components
  override val backupDir: Path = innerSettings.backupDir
  override val copyOptions: Array[CopyOption] = innerSettings.copyOptions
}
