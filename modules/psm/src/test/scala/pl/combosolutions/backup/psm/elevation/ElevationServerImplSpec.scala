package pl.combosolutions.backup.psm.elevation

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.psm.programs.{ Result, GenericProgram }
import pl.combosolutions.backup.test.Tags.UnitTest
import pl.combosolutions.backup.test.{ Tags, AsyncResultSpecificationHelper }

class ElevationServerImplSpec extends Specification with Mockito with AsyncResultSpecificationHelper {

  val server: ElevationServer = new ElevationServerImpl

  "ElevationServerImpl" should {

    "execute GenericPrograms passed into it" in {
      // given
      val program = mock[GenericProgram]
      val expected = Result[GenericProgram](0, List(), List())
      program.run returns (AsyncResult some expected)

      // when
      val result = server runRemote program

      // then
      result must beSome(expected)
    } tag UnitTest
  }
}
