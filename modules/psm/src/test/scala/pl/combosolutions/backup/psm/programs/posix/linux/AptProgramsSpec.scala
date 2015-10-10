package pl.combosolutions.backup.psm.programs.posix.linux

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.{ Async, Result }
import pl.combosolutions.backup.psm.programs.{ Program, TestProgramHelper }
import pl.combosolutions.backup.psm.repositories.posix.linux.AptRepositoriesServiceComponent._
import pl.combosolutions.backup.psm.repositories.{ VersionedPackage, AptRepository }
import pl.combosolutions.backup.test.Tags.UnitTest

class AptProgramsSpec extends Specification with Mockito {

  val repository = AptRepository(false, "test-url", "test-branch", List("test"), List("test"))
  val package_ = VersionedPackage("test-program", "test-version")

  "AptAddRepository" should {

    "create apt-add-repository command" in {
      // given
      // when
      val program = AptAddRepository(repository)

      // then
      program.name mustEqual "apt-add-repository"
      program.arguments mustEqual List("--yes", repository toString)
    } tag UnitTest

    "be digested to Boolean with built-in Interpreter" in {
      // given
      import AptPrograms.AptAddRepository2Boolean
      val program = new AptAddRepository(repository) with TestProgramHelper[AptAddRepository]

      // when
      val result = program.digest[Boolean]

      // then
      result must beSome(true).await
    } tag UnitTest
  }

  "AptRemoveRepository" should {

    "create apt-remove-repository command" in {
      // given
      // when
      val program = AptRemoveRepository(repository)

      // then
      program.name mustEqual "apt-add-repository"
      program.arguments mustEqual List("--yes", "--remove", repository toString)
    } tag UnitTest

    "be digested to Boolean with built-in Interpreter" in {
      // given
      import AptPrograms.AptRemoveRepository2Boolean
      val program = new AptRemoveRepository(repository) with TestProgramHelper[AptRemoveRepository]

      // when
      val result = program.digest[Boolean]

      // then
      result must beSome(true).await
    } tag UnitTest
  }

  "AptGetInstall" should {

    "create apt-get install command" in {
      // given
      // when
      val program = AptGetInstall(List(package_))

      // then
      program.name mustEqual "apt-get"
      program.arguments mustEqual List("install", "-y", "-qq", package_.name)
    } tag UnitTest

    "be digested to Boolean with built-in Interpreter" in {
      // given
      import AptPrograms.AptGetInstall2Boolean
      val program = new AptGetInstall(List(package_)) with TestProgramHelper[AptGetInstall]

      // when
      val result = program.digest[Boolean]

      // then
      result must beSome(true).await
    } tag UnitTest
  }

  "AptGetRemove" should {

    "create apt-get remove command" in {
      // given
      // when
      val program = AptGetRemove(List(package_))

      // then
      program.name mustEqual "apt-get"
      program.arguments mustEqual List("remove", "-y", "-qq", package_.name)
    } tag UnitTest

    "be digested to Boolean with built-in Interpreter" in {
      // given
      import AptPrograms.AptGetRemove2Boolean
      val program = new AptGetRemove(List(package_)) with TestProgramHelper[AptGetRemove]

      // when
      val result = program.digest[Boolean]

      // then
      result must beSome(true).await
    } tag UnitTest
  }

  "AptGetUpdate" should {

    "create apt-get update command" in {
      // given
      // when
      val program = AptGetUpdate

      // then
      program.name mustEqual "apt-get"
      program.arguments mustEqual List("update", "-y", "-qq")
    } tag UnitTest

    "be digested to Boolean with built-in Interpreter" in {
      // given
      import AptPrograms.AptGetUpdate2Boolean
      val program = new Program[AptGetUpdate]("", List()) with TestProgramHelper[AptGetUpdate]

      // when
      val result = program.digest[Boolean]

      // then
      result must beSome(true).await
    } tag UnitTest
  }

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
      import AptPrograms.DpkgList2VersionedPackages
      val program = new Program[DpkgList]("", List()) with TestProgramHelper[DpkgList]
      program.result = Async some Result[DpkgList](0, List("ii test-package test-version"), List())

      // when
      val result = program.digest[List[VersionedPackage]]

      // then
      result must beSome(List(VersionedPackage("test-package", "test-version"))).await
    } tag UnitTest
  }

  "ListAptRepos" should {

    "create grep ^deb command" in {
      // given
      // when
      val program = ListAptRepos

      // then
      program.name mustEqual "grep"
      program.arguments mustEqual List("-h", "^deb", etcAptSourcesMain, etcAptSourcesDir)
    } tag UnitTest

    "be digested to List[AptRepository] with built-in Interpreter" in {
      // given
      import AptPrograms.ListAptRepos2AptRepositories
      val expected = AptRepository(false, "test-package", "test-version", List(), List())
      val program = new Program[ListAptRepos]("", List()) with TestProgramHelper[ListAptRepos]
      program.result = Async some Result[ListAptRepos](0, List(s"/etc/apt/sources.list:$expected"), List())

      // when
      val result = program.digest[List[AptRepository]]

      // then
      result must beSome(List(expected)).await
    } tag UnitTest
  }
}
