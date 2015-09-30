package pl.combosolutions.backup.psm.elevation

import java.rmi.registry.Registry

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.psm.programs.{ Result, GenericProgram }
import pl.combosolutions.backup.test.AsyncResultSpecificationHelper

import scala.sys.process.Process

class ElevationFacadeSpec extends Specification with Mockito with AsyncResultSpecificationHelper {

  val remotePort: Integer = 6666
  val remoteName = "test"

  "ElevationFacade" should {

    "run program on a remote server" in {
      // given
      val rmiManager = mock[RmiManager]
      val registry = mock[Registry]
      val notifier = mock[ElevationReadyNotifier]
      val server = mock[Process]
      val client = mock[ElevationClient]
      val program = GenericProgram("test", List())
      val expected = Result[GenericProgram](0, List(), List())
      rmiManager.createRegister returns ((registry, remotePort))
      (rmiManager findFreeName ===(registry)) returns remoteName
      (rmiManager createReadyNotifier (===(remoteName), ===(registry), any[RmiMutex])) returns notifier
      (rmiManager createServer (===(remoteName), ===(remoteName), ===(remotePort))) returns server
      (rmiManager createClient (===(remoteName), ===(remotePort))) returns client
      (client executeRemote program) returns (AsyncResult some expected)

      // when
      val facade = new TestElevationFacade(rmiManager)
      val result = facade runRemotely program

      // then
      await(result) must beSome(expected)
    }

    "close elevated executor" in {
      // given
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

      // when
      val facade = new TestElevationFacade(rmiManager)
      facade.close

      // then
      there was one(client).terminate
      there was one(server).destroy
    }
  }

  class TestElevationFacade(rmiManager: RmiManager) extends ElevationFacade(rmiManager) {

    override def createMutex = new RmiMutex {

      override def waitForReadiness = {}
    }

    override def waitForReadiness = {}
  }
}
