package pl.combosolutions.backup.psm

import pl.combosolutions.backup.psm.elevation.TestElevationServiceComponent
import pl.combosolutions.backup.psm.filesystem.TestFileSystemServiceComponent
import pl.combosolutions.backup.psm.repositories.TestRepositoriesServiceComponent
import pl.combosolutions.backup.psm.systems.TestOperatingSystemComponent

trait TestComponentsHelper extends ComponentRegistry
  with TestOperatingSystemComponent
  with TestElevationServiceComponent
  with TestFileSystemServiceComponent
  with TestRepositoriesServiceComponent
