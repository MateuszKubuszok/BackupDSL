package pl.combosolutions.backup.psm.repositories.posix.linux

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, ObligatoryElevationMode, TestElevationFacadeComponent }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.psm.programs.posix.linux._
import pl.combosolutions.backup.psm.programs.posix.linux.AptPrograms._
import pl.combosolutions.backup.psm.repositories.{ AptRepository, UnversionedPackage, VersionedPackage }
import pl.combosolutions.backup.test.AsyncResultSpecificationHelper

class AptRepositoriesSpec extends Specification with Mockito with AsyncResultSpecificationHelper {

  val component = new AptRepositoriesServiceComponent with TestElevationFacadeComponent
  val service = component.repositoriesService

  "AptRepositoriesService" should {

    "obtain available repositories" in {
      // given
      type ProgramType = Program[ListAptRepos]
      type ResultType = List[AptRepository]
      type InterpreterType = ListAptReposInterpreter[ResultType]
      val repository = AptRepository(true, "test", "test", List(), List())
      val repositories = List(repository)
      val program = mock[ProgramType]
      implicit val elevationMode = mock[ElevationMode]
      implicit val cleaner = new Cleaner {}
      program.digest[ResultType](any[InterpreterType]) returns AsyncResult.some(repositories)
      elevationMode(any[ProgramType], ===(cleaner)) returns program

      // when
      val result = service.obtainRepositories

      // then
      await(result) must beSome(repositories)
    }

    "add repositories" in {
      // given
      type ProgramType = Program[AptAddRepository]
      type ResultType = Boolean
      type InterpreterType = AptAddRepositoryInterpreter[ResultType]
      val repository = AptRepository(true, "test", "test", List(), List())
      val repositories = List(repository)
      val program = mock[ProgramType]
      implicit val elevationMode = mock[ObligatoryElevationMode]
      implicit val cleaner = new Cleaner {}
      program.digest[ResultType](any[InterpreterType]) returns AsyncResult.some(true)
      elevationMode(any[ProgramType], ===(cleaner)) returns program

      // when
      val result = service addRepositories repositories

      // then
      await(result) must beSome(true)
    }

    "remove repositories" in {
      // given
      type ProgramType = Program[AptRemoveRepository]
      type ResultType = Boolean
      type InterpreterType = AptRemoveRepositoryInterpreter[ResultType]
      val repository = AptRepository(true, "test", "test", List(), List())
      val repositories = List(repository)
      val program = mock[ProgramType]
      implicit val elevationMode = mock[ObligatoryElevationMode]
      implicit val cleaner = new Cleaner {}
      program.digest[ResultType](any[InterpreterType]) returns AsyncResult.some(true)
      elevationMode(any[ProgramType], ===(cleaner)) returns program

      // when
      val result = service removeRepositories repositories

      // then
      await(result) must beSome(true)
    }

    "install packages" in {
      // given
      type ProgramType = Program[AptGetInstall]
      type ResultType = Boolean
      type InterpreterType = AptGetInstallInterpreter[ResultType]
      val package_ = UnversionedPackage("test")
      val packages = List(package_)
      val program = mock[ProgramType]
      implicit val elevationMode = mock[ObligatoryElevationMode]
      implicit val cleaner = new Cleaner {}
      program.digest[ResultType](any[InterpreterType]) returns AsyncResult.some(true)
      elevationMode(any[ProgramType], ===(cleaner)) returns program

      // when
      val result = service installAll packages

      // then
      await(result) must beSome(true)
    }

    "check if all packages are installed" in {
      // given
      type ProgramType = Program[DpkgList]
      type ResultType = List[VersionedPackage]
      type InterpreterType = DpkgListInterpreter[ResultType]
      val package_ = VersionedPackage("test", "test")
      val packages = List(package_)
      val program = mock[ProgramType]
      implicit val elevationMode = mock[ObligatoryElevationMode]
      implicit val cleaner = new Cleaner {}
      program.digest[ResultType](any[InterpreterType]) returns AsyncResult.some(packages)
      elevationMode(any[ProgramType], ===(cleaner)) returns program

      // when
      val result = service areAllInstalled packages

      // then
      await(result) must beSome(true)
    }
  }
}
