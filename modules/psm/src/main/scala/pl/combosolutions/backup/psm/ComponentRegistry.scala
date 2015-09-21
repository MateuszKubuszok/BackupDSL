package pl.combosolutions.backup.psm

import pl.combosolutions.backup.Logging
import pl.combosolutions.backup.psm.elevation.{ ElevationServiceComponent, ElevationServiceComponentImpl }
import pl.combosolutions.backup.psm.filesystem.{ FileSystemServiceComponent, FileSystemServiceComponentImpl }
import pl.combosolutions.backup.psm.repositories.{ RepositoriesServiceComponent, RepositoriesServiceComponentImpl }
import pl.combosolutions.backup.psm.systems.{ OperatingSystemComponentImpl, OperatingSystemComponent }

trait ComponentRegistry extends AnyRef
  with OperatingSystemComponent
  with ElevationServiceComponent
  with FileSystemServiceComponent
  with RepositoriesServiceComponent

trait ComponentsHelper extends ComponentRegistry
    with OperatingSystemComponentImpl
    with ElevationServiceComponentImpl
    with FileSystemServiceComponentImpl
    with RepositoriesServiceComponentImpl { self: ComponentRegistry =>
}
