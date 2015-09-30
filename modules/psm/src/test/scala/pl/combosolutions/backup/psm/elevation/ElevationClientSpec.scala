package pl.combosolutions.backup.psm.elevation

import java.rmi.RemoteException

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.programs.{ Result, GenericProgram }
import pl.combosolutions.backup.test.Tags.UnitTest
import pl.combosolutions.backup.test.{ Tags, AsyncResultSpecificationHelper }

class ElevationClientSpec extends Specification with Mockito with AsyncResultSpecificationHelper {

  private val name = "mock-repository"
  private val remotePort = 6000
  private val program = GenericProgram("test-program", List())

  "ElevationClient" should {

    "return Some successful AsyncResult for Some successful response" in {
      // given
      val server = mock[ElevationServer]
      val client = elevationClientFor(server)
      val expected = Result[GenericProgram](0, List(), List())
      (server runRemote program) returns Some(expected)

      // when
      val result = await(client executeRemote program)

      // then
      result must beSome(expected)
    } tag UnitTest

    "return None successful AsyncResult for None successful response" in {
      // given
      val server = mock[ElevationServer]
      val client = elevationClientFor(server)
      (server runRemote program) returns None

      // when
      val result = await(client executeRemote program)

      // then
      result must beNone
    } tag UnitTest

    "return failed AsyncResult for failed request" in {
      // given
      val server = mock[ElevationServer]
      val client = elevationClientFor(server)
      (server runRemote program) throws (new RemoteException("test exception"))

      // when
      val result = (client executeRemote program)

      // then
      result must throwA[RemoteException].await
    } tag UnitTest

    "terminate server on termination command" in {
      // given
      val server = mock[ElevationServer]
      val client = elevationClientFor(server)

      // when
      client.terminate

      // then
      there was one(server).terminate
    } tag UnitTest
  }

  private def elevationClientFor(mockServer: ElevationServer) = new ElevationClient(name, remotePort) {

    override protected def server = mockServer
  }
}
