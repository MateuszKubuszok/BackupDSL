package pl.combosolutions.backup.psm.repositories.posix.linux

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, TestElevationFacadeComponent }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.psm.programs.posix.linux.AptPrograms.ListAptReposInterpreter
import pl.combosolutions.backup.psm.programs.posix.linux.ListAptRepos
import pl.combosolutions.backup.psm.repositories.AptRepository
import pl.combosolutions.backup.test.AsyncResultSpecificationHelper

class AptRepositoriesSpec extends Specification with Mockito with AsyncResultSpecificationHelper {

  val component = new AptRepositoriesServiceComponent with TestElevationFacadeComponent
  val service = component.repositoriesService

  "AptRepositoriesService" should {

    "obtain available repositories" in {
      // given
      type InterpreterType = ListAptReposInterpreter[List[AptRepository]]
      val repository = AptRepository(true, "test", "test", List(), List())
      val repositories = List(repository)
      val program = mock[Program[ListAptRepos]]
      implicit val elevationMode = mock[ElevationMode]
      implicit val cleaner = new Cleaner {}
      program.digest[List[AptRepository]](any[InterpreterType]) returns AsyncResult.some(repositories)
      elevationMode(any[Program[ListAptRepos]], ===(cleaner)) returns program

      // when
      val result = service.obtainRepositories

      // then
      await(result) must beSome(repositories)
    }
  }
}
