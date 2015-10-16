package pl.combosolutions.backup.tasks

import java.nio.file.Path
import pl.combosolutions.backup.Cleaner
import pl.combosolutions.backup.psm.ComponentRegistry
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, NotElevated, ObligatoryElevationMode, RemoteElevation }
import pl.combosolutions.backup.tasks.DefaultsAndConstants.BackupDirPath

object Settings {

  def apply(settings: Settings): ImmutableSettings = ImmutableSettings(
    cleaner                 = settings.cleaner,
    withElevation           = settings.withElevation,
    withObligatoryElevation = settings.withObligatoryElevation,
    components              = settings.components,
    backupDir               = settings.backupDir
  )
}

trait Settings {

  def cleaner: Cleaner
  def withElevation: ElevationMode
  def withObligatoryElevation: ObligatoryElevationMode
  def components: ComponentRegistry
  def backupDir: Path
}

case class ImmutableSettings(
  override val cleaner:                 Cleaner,
  override val withElevation:           ElevationMode           = NotElevated,
  override val withObligatoryElevation: ObligatoryElevationMode = RemoteElevation,
  override val components:              ComponentRegistry       = ComponentRegistry,
  override val backupDir:               Path                    = BackupDirPath
) extends Settings

case class MutableSettings(
    var innerSettings: Option[Settings]
) extends Settings {

  override def cleaner: Cleaner = innerSettings.get.cleaner
  override def withElevation: ElevationMode = innerSettings.get.withElevation
  override def withObligatoryElevation: ObligatoryElevationMode = innerSettings.get.withObligatoryElevation
  override def components: ComponentRegistry = innerSettings.get.components
  override def backupDir: Path = innerSettings.get.backupDir
}
