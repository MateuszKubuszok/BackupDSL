package pl.combosolutions.backup.psm.elevation

import org.specs2.mock.Mockito
import pl.combosolutions.backup.psm.operations.Cleaner

trait TestElevationFacadeComponent extends ElevationFacadeComponent with Mockito {

  val mockElevationFacade = mock[ElevationFacade]

  override def elevationFacadeFor(cleaner: Cleaner) = mockElevationFacade
}
