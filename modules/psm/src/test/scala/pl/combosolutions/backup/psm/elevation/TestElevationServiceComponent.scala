package pl.combosolutions.backup.psm.elevation

import org.specs2.mock.Mockito

trait TestElevationServiceComponent extends ElevationServiceComponent with Mockito {

  val testElevationService = mock[ElevationService]

  override def elevationService = testElevationService
}
