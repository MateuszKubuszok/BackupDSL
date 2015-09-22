package pl.combosolutions.backup.psm.systems

import org.specs2.mock.Mockito

trait TestOperatingSystemComponent extends OperatingSystemComponent with Mockito {

  val testOperatingSystem = mock[OperatingSystem]

  override def operatingSystem = testOperatingSystem
}
