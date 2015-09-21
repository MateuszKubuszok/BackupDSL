package pl.combosolutions.backup.dsl.tasks

import BackupIniConfig._

object BackupIniConfig {

  type IniConfigs = Map[String, List[String]]
  type BackupResult = IniConfigs
  type RestoreResult = IniConfigs
}

abstract case class BackupIniConfig[PBR, PRR](configs: IniConfigs)
    extends Task[PBR, PRR, BackupResult, RestoreResult]("backup ini file") {
  // TODO implementation
}

