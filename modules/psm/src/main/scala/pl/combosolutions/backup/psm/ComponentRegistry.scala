package pl.combosolutions.backup.psm

import pl.combosolutions.backup.Logging
import pl.combosolutions.backup.psm
import psm.elevation.{ ElevationService, ElevationServiceComponent, ElevationServiceComponentImpl }
import psm.filesystem.{ FileSystemService, FileSystemServiceComponent, FileSystemServiceComponentImpl }
import psm.repositories.{ RepositoriesService, RepositoriesServiceComponent, RepositoriesServiceComponentImpl }
import psm.systems.{ OperatingSystem, OperatingSystemComponent, OperatingSystemComponentImpl }

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

    override def operatingSystem: OperatingSystem = ComponentRegistry.operatingSystem
    override def elevationService: ElevationService = ComponentRegistry.elevationService
    override def fileSystemService: FileSystemService = ComponentRegistry.fileSystemService
    override def repositoriesService: RepositoriesService = ComponentRegistry.repositoriesService
  }
}

import pl.combosolutions.backup.psm.ComponentRegistry.ComponentRegistryImpl

trait ComponentsHelper extends ComponentRegistry with ComponentRegistryImpl
