package pl.combosolutions.backup.dsl.tasks

import BackupPackages._
import pl.combosolutions.backup.psm.operations.PlatformSpecific

object BackupPackages {
  type Packages = PlatformSpecific.current.Packages
  type Repositories = PlatformSpecific.current.Repositories
  type BackupResult = (Repositories, Packages)
  type RestoreResult = (Repositories, Packages)
}

abstract case class BackupPackages[PBR, PRR](
    includeRepositories: Packages = List(),
    excludeRepositories: Packages = List(),
    includePackages: Packages = List(),
    excludePackages: Packages = List()) extends Task[PBR, PRR, BackupResult, RestoreResult]("backup packages") {
  // TODO implementation
}
