package pl.combosolutions.backup.psm.elevation

import java.rmi.registry.Registry

import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.commands.TestCommand
import pl.combosolutions.backup.{ Async, Result }
import pl.combosolutions.backup.psm.programs.GenericProgram
import pl.combosolutions.backup.test.AsyncSpecificationHelper
import pl.combosolutions.backup.test.Tags.UnitTest

import scala.sys.process.Process

class ElevationFacadeSpec extends Specification with Mockito with AsyncSpecificationHelper {

  val remotePort: Integer = 6666
  val remoteName = "test"

  "ElevationFacade" should {

    "run command on a remote server" in new TestContext {
      // given
      val expected = Result[GenericProgram](0, List(), List())
      val command = TestCommand(expected)
      (client executeRemote command) returns (Async some expected.asSpecific)

      // when
      val facade = new TestElevationFacade(rmiManager)
      val result = facade runRemotely command

      // then
      await(result) must beSome(expected)
    } tag UnitTest

    "run program on a remote server" in new TestContext {
      // given
      val program = GenericProgram("test", List())
      val expected = Result[GenericProgram](0, List(), List())
      (client executeRemote program) returns (Async some expected)

      // when
      val facade = new TestElevationFacade(rmiManager)
      val result = facade runRemotely program

      // then
      await(result) must beSome(expected)
    } tag UnitTest

    "close elevated executor" in new TestContext {
      // given
      // when
      val facade = new TestElevationFacade(rmiManager)
      facade.close

      // then
      there was one(client).terminate
      there was one(server).destroy
    } tag UnitTest
  }

  class TestElevationFacade(rmiManager: RmiManager) extends ElevationFacade(rmiManager) {

    override def createMutex = new RmiMutex {

      override def waitForReadiness = {}
    }

    override def waitForReadiness = {}
  }

  trait TestContext extends Scope {

    val rmiManager = mock[RmiManager]
    val registry = mock[Registry]
    val notifier = mock[ElevationReadyNotifier]
    val server = mock[Process]
    val client = mock[ElevationClient]

    rmiManager.createRegister returns ((registry, remotePort))

    (rmiManager findFreeName ===(registry)) returns remoteName
    (rmiManager createReadyNotifier (===(remoteName), ===(registry), any[RmiMutex])) returns notifier
    (rmiManager createServer (===(remoteName), ===(remoteName), ===(remotePort))) returns server
    (rmiManager createClient (===(remoteName), ===(remotePort))) returns client
  }
}
