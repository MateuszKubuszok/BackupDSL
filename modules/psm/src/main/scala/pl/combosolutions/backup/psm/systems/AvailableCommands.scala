package pl.combosolutions.backup.psm.systems

import pl.combosolutions.backup.psm.programs.posix.PosixPrograms._
import pl.combosolutions.backup.psm.programs.posix.WhichProgram

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait AvailableCommands {

  val aptGet: Boolean

  val gkSudo: Boolean

  val kdeSudo: Boolean
}

trait AvailableCommandsComponent {
  self: AvailableCommandsComponent with OperatingSystemComponent =>

  def availableCommands: AvailableCommands
}

object AvailableCommandsComponentImpl extends AvailableCommandsComponent with OperatingSystemComponentImpl {
  self: AvailableCommandsComponent with OperatingSystemComponent =>

  override def availableCommands: AvailableCommands = AvailableCommandsImpl

  trait AvailableCommandsImpl extends AvailableCommands {

    override lazy val aptGet = operatingSystem.isPosix && posixAvailable("apt-get")

    override lazy val gkSudo = operatingSystem.isPosix && posixAvailable("gksudo")

    override lazy val kdeSudo = operatingSystem.isPosix && posixAvailable("kdesudo")

    private def posixAvailable(programName: String): Boolean =
      Await.result(WhichProgram(programName).digest[Boolean], Duration.Inf) getOrElse false
  }

  object AvailableCommandsImpl extends AvailableCommandsImpl
}

trait AvailableCommandsComponentImpl extends AvailableCommandsComponent with OperatingSystemComponent {

  override lazy val availableCommands = AvailableCommandsComponentImpl.availableCommands
}
