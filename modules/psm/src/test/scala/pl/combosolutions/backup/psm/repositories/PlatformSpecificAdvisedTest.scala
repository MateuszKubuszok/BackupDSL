package pl.combosolutions.backup.psm.repositories

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.elevation.RemoteElevation
import pl.combosolutions.backup.psm.repositories.posix.linux.AptRepositoriesServiceComponent.AptRepositoriesService
import pl.combosolutions.backup.test.ElevationTestHelper
import pl.combosolutions.backup.test.Tags.PlatformTest

class PlatformSpecificAdvisedTest
    extends Specification
    with Mockito
    with ElevationTestHelper
    with ComponentsHelper {

  sequential

  type Repositories = List[Repository]

  val testRepository: Repository = repositoriesService match {
    case repository: AptRepositoriesService => AptRepository(false, "test-url", "test-branch", List("test"), List("x368"))
    case _ => ReportException onNotImplemented "Unknown repository"
  }

  "Current platform's repositories" should {

    "obtains repositories" in {
      // given
      implicit val withElevation = RemoteElevation
      implicit val cleaner = ElevationTestCleaner

      // when
      val result = repositoriesService.obtainRepositories

      // then
      result must beSome.await
    } tag PlatformTest

    "adds test repository" in {
      // given
      implicit val withElevation = RemoteElevation
      implicit val cleaner = ElevationTestCleaner
      val repositories = List(testRepository)

      // when
      val result = repositoriesService addRepositories repositories

      // then
      result must beSome(true).await
    } tag PlatformTest

    "removes test repository" in {
      // given
      implicit val withElevation = RemoteElevation
      implicit val cleaner = ElevationTestCleaner
      val repositories = List(testRepository)

      // when
      val result = repositoriesService removeRepositories repositories

      // then
      result must beSome(true).await
    } tag PlatformTest
  }
}
