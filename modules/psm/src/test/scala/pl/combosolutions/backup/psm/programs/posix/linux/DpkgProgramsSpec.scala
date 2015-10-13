package pl.combosolutions.backup.psm.programs.posix.linux

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.{ Async, Result }
import pl.combosolutions.backup.psm.repositories.VersionedPackage
import pl.combosolutions.backup.psm.programs.{ Program, TestProgramHelper }
import pl.combosolutions.backup.test.Tags.UnitTest

class DpkgProgramsSpec extends Specification with Mockito {

  "DpkgList" should {

    "create dpkg command" in {
      // given
      // when
      val program = DpkgList

      // then
      program.name mustEqual "dpkg"
      program.arguments mustEqual List("--list")
    } tag UnitTest

    "be digested to List[VersionedPackage] with built-in Interpreter" in {
      // given
      import DpkgPrograms.DpkgList2VersionedPackages
      val program = new Program[DpkgList]("", List()) with TestProgramHelper[DpkgList]
      program.result = Async some Result[DpkgList](0, List("ii test-package test-version"), List())

      // when
      val result = program.digest[List[VersionedPackage]]

      // then
      result must beSome(List(VersionedPackage("test-package", "test-version"))).await
    } tag UnitTest
  }
}
