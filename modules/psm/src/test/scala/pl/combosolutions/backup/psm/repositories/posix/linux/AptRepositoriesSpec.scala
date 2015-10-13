package pl.combosolutions.backup.psm.repositories.posix.linux

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.{ Async, Result }
import pl.combosolutions.backup.psm.elevation.{ ObligatoryElevationMode, TestElevationFacadeComponent }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.{ ProgramContextHelper, Program }
import pl.combosolutions.backup.psm.programs.posix.linux._
import pl.combosolutions.backup.psm.repositories.{ AptRepository, UnversionedPackage, VersionedPackage }
import pl.combosolutions.backup.test.AsyncSpecificationHelper
import pl.combosolutions.backup.test.Tags.UnitTest

import scala.reflect.ClassTag

class AptRepositoriesSpec
    extends Specification
    with Mockito
    with AsyncSpecificationHelper
    with ProgramContextHelper {

  val component = new AptRepositoriesServiceComponent with TestElevationFacadeComponent
  val service = component.repositoriesService

  "AptRepositoriesService" should {

    "obtain available repositories" in new ProgramContext(classOf[ListAptRepos], classOf[List[AptRepository]]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val repository = AptRepository(true, "test", "test", List(), List())
      val repositories = List(repository)
      makeDigestReturn(repositories)

      // when
      val result = service.obtainRepositories

      // then
      await(result) must beSome(repositories)
    } tag UnitTest

    "add repositories" in new ProgramContext(classOf[AptAddRepository], classOf[Boolean]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val repository = AptRepository(true, "test", "test", List(), List())
      val repositories = List(repository)
      makeDigestReturn(true)

      // when
      val result = service addRepositories repositories

      // then
      await(result) must beSome(true)
    } tag UnitTest

    "remove repositories" in new ProgramContext(classOf[AptRemoveRepository], classOf[Boolean]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val repository = AptRepository(true, "test", "test", List(), List())
      val repositories = List(repository)
      makeDigestReturn(true)

      // when
      val result = service removeRepositories repositories

      // then
      await(result) must beSome(true)
    } tag UnitTest

    "install packages" in new ProgramContext(classOf[AptGetInstall], classOf[Boolean]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val package_ = UnversionedPackage("test")
      val packages = List(package_)
      makeDigestReturn(true)

      // when
      val result = service installAll packages

      // then
      await(result) must beSome(true)
    } tag UnitTest

    "check if all packages are installed" in new ProgramContext(classOf[DpkgList], classOf[List[VersionedPackage]]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val package_ = VersionedPackage("test", "test")
      val packages = List(package_)
      makeDigestReturn(packages)

      // when
      val result = service areAllInstalled packages

      // then
      await(result) must beSome(true)
    } tag UnitTest
  }
}
