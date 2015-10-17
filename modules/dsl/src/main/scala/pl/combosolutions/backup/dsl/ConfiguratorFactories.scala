package pl.combosolutions.backup.dsl

import java.nio.file.Path

import pl.combosolutions.backup.tasks.{ Configurator, Settings }

trait SpawnConfigurator {

  val initialSettings: Settings
}

trait SpawnSelectFiles[PBR, CBR >: List[Path], PRR, CRP >: List[Path]] extends SpawnConfigurator {
  self: Configurator[PBR, _, CBR, PRR, _, CRP] =>

  def selectFiles: SelectFiles[PBR, PRR] =
    new SelectFiles[PBR, PRR](this.adjustForChildren[List[Path], List[Path]], initialSettings)
}

trait SpawnBackupFiles extends SpawnConfigurator {
  self: Configurator[List[Path], _, List[Path], List[Path], _, List[Path]] =>

  def backupFiles[CBR, CRR]: BackupFiles[CBR, CRR] = new BackupFiles[CBR, CRR](this, initialSettings)
}
