package pl.combosolutions.backup.psm.elevation

import java.rmi.RemoteException
import java.rmi.registry.Registry

import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.test.Tags.UnitTest

class ElevatedExecutorSpec extends Specification with Mockito {

  val notifierName = "notifier-test-name"
  val serverName = "server-test-name"
  val remotePort = 666

  val arguments = new Array[String](3)
  arguments.update(0, notifierName)
  arguments.update(1, serverName)
  arguments.update(2, remotePort.toString)

  "ElevatedExecutor" should {

    "export ElevationServer and notify about successful initialization" in new TestContext {
      // given
      // when
      new TestElevatedExecutor(registry)(arguments)

      // then
      there was one(registry).bind(===(serverName), any[ElevationServer])
      there was one(notifier).notifyReady
    } tag UnitTest

    "notify about failed initialization" in new TestContext {
      // given
      (registry.bind(===(serverName), any[ElevationServer])) throws (new RemoteException)

      // when
      new TestElevatedExecutor(registry)(arguments)

      // then
      there was one(registry).bind(===(serverName), any[ElevationServer])
      there was one(notifier).notifyFailure
    } tag UnitTest
  }

  class TestElevatedExecutor(mockRegistry: Registry)(args: Array[String])
      extends ElevatedExecutor(args) {

    override def configureRMI = {}

    override def locateRegistryFor(remotePort: Integer) = mockRegistry

    override def exportServer(server: ElevationServer) = server

    override def terminateOnFailure = {}
  }

  trait TestContext extends Scope {

    val registry = mock[Registry]
    val notifier = mock[ElevationReadyNotifier]

    (registry lookup notifierName) returns notifier
  }
}
