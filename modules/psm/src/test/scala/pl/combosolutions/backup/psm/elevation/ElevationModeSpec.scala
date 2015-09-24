package pl.combosolutions.backup.psm.elevation

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.GenericProgram

class ElevationModeSpec extends Specification with Mockito {

  val program = GenericProgram("test", List())
  val expected = GenericProgram("returned", List())

  "NotElevated" should {

    "not elevate passed program" in {
      // given
      val cleaner = new Cleaner {}
      val mode = NotElevated

      // when
      val result = mode(program, cleaner)

      // then
      result mustEqual program
    }
  }

  "DirectElevation" should {

    "create directly elevated program" in {
      // given
      val cleaner = new Cleaner {}
      val mode = new DirectElevation with TestElevationServiceComponent
      (mode.testElevationService elevateDirect program) returns expected

      // when
      val result = mode(program, cleaner)

      // then
      result mustEqual expected
      there was one(mode.testElevationService).elevateDirect(===(program))
    }
  }

  "RemoteElevation" should {

    "create remotely elevated program" in {
      // given
      val cleaner = new Cleaner {}
      val mode = new RemoteElevation with TestElevationServiceComponent
      mode.testElevationService.elevateRemote(program, cleaner) returns expected

      // when
      val result = mode(program, cleaner)

      // then
      result mustEqual expected
      there was one(mode.testElevationService).elevateRemote(===(program), ===(cleaner))
    }
  }

  "ElevateIfNeeded" should {

    "run ElevationMode from implicit context" in {
      // given
      import ElevateIfNeeded._
      implicit val mode = mock[ElevationMode]
      implicit val cleaner = new Cleaner {}
      mode.apply(===(program), ===(cleaner)) returns expected

      // when
      val result = program.handleElevation

      // then
      result mustEqual expected
    }
  }
}
