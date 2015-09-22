package pl.combosolutions.backup.psm

import pl.combosolutions.backup.Logging
import pl.combosolutions.backup.psm.elevation.{ ElevationServiceComponent, ElevationServiceComponentImpl }
import pl.combosolutions.backup.psm.filesystem.{ FileSystemServiceComponent, FileSystemServiceComponentImpl }
import pl.combosolutions.backup.psm.repositories.{ RepositoriesServiceComponent, RepositoriesServiceComponentImpl }
import pl.combosolutions.backup.psm.systems.{ OperatingSystemComponent, OperatingSystemComponentImpl }

trait ComponentRegistry extends AnyRef
  with OperatingSystemComponent
  with ElevationServiceComponent
  with FileSystemServiceComponent
  with RepositoriesServiceComponent

object ComponentRegistry extends ComponentRegistry
    with OperatingSystemComponentImpl
    with ElevationServiceComponentImpl
    with FileSystemServiceComponentImpl
    with RepositoriesServiceComponentImpl
    with Logging {

  logger trace s"Operating System:     ${operatingSystem.getClass.getSimpleName}"
  logger trace s"Elevation Service:    ${elevationService.getClass.getSimpleName}"
  logger trace s"File System Service:  ${fileSystemService.getClass.getSimpleName}"
  logger trace s"Repositories Service: ${repositoriesService.getClass.getSimpleName}"

  trait ComponentRegistryImpl extends ComponentRegistry {

    override def operatingSystem = ComponentRegistry.operatingSystem
    override def elevationService = ComponentRegistry.elevationService
    override def fileSystemService = ComponentRegistry.fileSystemService
    override def repositoriesService = ComponentRegistry.repositoriesService
  }
}

import pl.combosolutions.backup.psm.ComponentRegistry.ComponentRegistryImpl

trait ComponentsHelper extends ComponentRegistry with ComponentRegistryImpl
