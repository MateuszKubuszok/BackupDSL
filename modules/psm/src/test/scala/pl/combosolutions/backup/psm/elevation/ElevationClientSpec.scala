package pl.combosolutions.backup.psm.elevation

import java.rmi.RemoteException

import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.Result
import pl.combosolutions.backup.psm.programs.GenericProgram
import pl.combosolutions.backup.test.Tags.UnitTest

class ElevationClientSpec extends Specification with Mockito {

  private val name = "mock-repository"
  private val remotePort = 6000
  private val program = GenericProgram("test-program", List())

  "ElevationClient" should {

    "return Some successful Async for Some successful response" in new TestContext {
      // given
      val expected = Result[GenericProgram](0, List(), List())
      (server runRemote program) returns Some(expected)

      // when
      val result = client executeRemote program

      // then
      result must beSome(expected).await
    } tag UnitTest

    "return None successful Async for None successful response" in new TestContext {
      // given
      (server runRemote program) returns None

      // when
      val result = client executeRemote program

      // then
      result must beNone.await
    } tag UnitTest

    "return failed Async for failed request" in new TestContext {
      // given
      server runRemote program throws new RemoteException("test exception")

      // when
      val result = client executeRemote program

      // then
      result must throwA[RemoteException].await
    } tag UnitTest

    "terminate server on termination command" in new TestContext {
      // given
      // when
      client.terminate

      // then
      there was one(server).terminate
    } tag UnitTest
  }

  private def elevationClientFor(mockServer: ElevationServer) = new ElevationClient(name, remotePort) {

    override protected def server = mockServer
  }

  trait TestContext extends Scope {

    val server = mock[ElevationServer]
    val client = elevationClientFor(server)
  }
}
