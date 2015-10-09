package pl.combosolutions.backup.psm.repositories

import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.elevation.RemoteElevation
import pl.combosolutions.backup.psm.repositories.posix.linux.AptRepositoriesServiceComponent.AptRepositoriesService
import pl.combosolutions.backup.test.ElevationTestHelper
import pl.combosolutions.backup.test.Tags.PlatformTest

import scala.concurrent.duration.DurationInt

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

  val testPackage: Package = repositoriesService match {
    case repository: AptRepositoriesService => UnversionedPackage("grep")
    case _ => ReportException onNotImplemented "Unknown repository"
  }

  val timeout = DurationInt(10) seconds

  "Current platform's repositories" should {

    "obtains repositories" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner

      // when
      val result = repositoriesService.obtainRepositories

      // then
      result must beSome.await
    } tag PlatformTest

    "adds test repository" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner

      // when
      val result = repositoriesService addRepositories repositories

      // then
      result must beSome(true).await
    } tag PlatformTest

    "removes test repository" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner

      // when
      val result = repositoriesService removeRepositories repositories

      // then
      result must beSome(true).await
    } tag PlatformTest

    "installs package" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner

      // when
      val result = repositoriesService installAll packages

      // then
      result must beSome(true).await(timeout = timeout)
    } tag PlatformTest

    "checks if all packages are installed" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner

      // when
      val result = repositoriesService areAllInstalled packages

      // then
      result must beSome(true).await(timeout = timeout)
    } tag PlatformTest
  }

  trait TestContext extends Scope {

    val withElevation = RemoteElevation
    val cleaner = ElevationTestCleaner
    val repositories = List(testRepository)
    val packages = List(testPackage)
  }
}
