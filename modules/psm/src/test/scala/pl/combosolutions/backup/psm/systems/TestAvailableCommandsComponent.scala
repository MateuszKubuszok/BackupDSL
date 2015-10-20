package pl.combosolutions.backup.psm.systems

import org.specs2.mock.Mockito

trait TestAvailableCommandsComponent
    extends AvailableCommandsComponent
    with OperatingSystemComponent
    with Mockito {

  val testAvailableCommands = mock[AvailableCommands]

  override def availableCommands = testAvailableCommands
}
