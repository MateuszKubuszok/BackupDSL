package pl.combosolutions.backup.dsl.tasks

import pl.combosolutions.backup.dsl.internals.operations.PlatformSpecific

import BackupPackages._

object BackupPackages {
  type Packages      = PlatformSpecific.current.Packages
  type Repositories  = PlatformSpecific.current.Repositories
  type BackupResult  = (Repositories, Packages)
  type RestoreResult = (Repositories, Packages)
}

abstract case class BackupPackages[PBR,PRR](
  includeRepositories: Packages = List(),
  excludeRepositories: Packages = List(),
  includePackages: Packages = List(),
  excludePackages: Packages = List()
) extends Task[PBR,PRR,BackupResult,RestoreResult] {
  // TODO implementation
}
