package pl.combosolutions.backup.dsl

import java.nio.file.Path

import pl.combosolutions.backup.tasks
import pl.combosolutions.backup.tasks.{ MutableConfigurator, Settings }

class Root(
  override val initialSettings: Settings
) extends tasks.RootConfigurator(initialSettings)
    with ConfiguratorUtils[Root] {

  def selectFiles = new SelectFiles[Unit, Unit](this.adjustForChildren[List[Path], List[Path]], settingsProxy)

  private[dsl] def buildTasks = build
}

class SelectFiles[PBR, PRR](
  parent:                       MutableConfigurator[PBR, _, List[Path], PRR, _, List[Path]],
  override val initialSettings: Settings
) extends tasks.SelectFilesConfigurator[PBR, List[Path], PRR, List[Path]](parent, initialSettings)
    with ConfiguratorUtils[SelectFiles[PBR, PRR]] {

  def backupFiles[CBR, CRR] = new BackupFiles[CBR, CRR](this, settingsProxy)
}

class BackupFiles[CBR, CRR](
  parent:                       MutableConfigurator[List[Path], _, List[Path], List[Path], _, List[Path]],
  override val initialSettings: Settings
) extends tasks.BackupFilesConfigurator[CBR, CRR](parent, initialSettings)
    with ConfiguratorUtils[BackupFiles[CBR, CRR]]
