package pl.combosolutions.backup.psm.commands

import pl.combosolutions.backup.{ Async, Result }

case class TestCommand(result: Result[_]) extends Command[TestCommand] {

  override def run: Async[Result[TestCommand]] = Async some result.asSpecific
}
