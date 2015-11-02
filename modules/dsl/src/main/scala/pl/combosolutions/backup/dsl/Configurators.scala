package pl.combosolutions.backup.dsl

import java.nio.file.Path

import pl.combosolutions.backup.{ Reporting, tasks }
import pl.combosolutions.backup.tasks.{ Configurator, Settings }

class Root(
  override val initialSettings: Settings
) extends tasks.RootConfigurator
    with ConfiguratorUtils[Root]
    with SpawnSelectFiles[Unit, Any, Unit, Any]
    with Reporting {

  reporter details "Initializing root task"

  private[dsl] def buildTasks = buildAll
}

class SelectFiles[PBR, PRR](
  parent:                       Configurator[PBR, _, List[Path], PRR, _, List[Path]],
  override val initialSettings: Settings
) extends tasks.SelectFilesConfigurator[PBR, List[Path], PRR, List[Path]](parent, initialSettings)
    with ConfiguratorUtils[SelectFiles[PBR, PRR]]
    with SpawnBackupFiles
    with Reporting {

  reporter details "Initializing select-files task"
}

class BackupFiles[CBR, CRR](
  parent:                       Configurator[List[Path], _, List[Path], List[Path], _, List[Path]],
  override val initialSettings: Settings
) extends tasks.BackupFilesConfigurator[CBR, CRR](parent, initialSettings)
    with ConfiguratorUtils[BackupFiles[CBR, CRR]]
    with Reporting {

  reporter details "Initializing backup-files task"
}
