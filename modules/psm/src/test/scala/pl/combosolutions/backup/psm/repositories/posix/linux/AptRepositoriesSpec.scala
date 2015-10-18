package pl.combosolutions.backup.psm.repositories.posix.linux

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.elevation.TestElevationFacadeComponent
import pl.combosolutions.backup.psm.programs.ProgramContextHelper
import pl.combosolutions.backup.psm.programs.posix.linux._
import pl.combosolutions.backup.psm.repositories._
import pl.combosolutions.backup.test.Tags.UnitTest

class AptRepositoriesSpec
    extends Specification
    with Mockito
    with ProgramContextHelper {

  val component = new AptRepositoriesServiceComponent with TestElevationFacadeComponent
  val service = component.repositoriesService

  "AptRepositoriesService" should {

    "obtain available repositories" in new ProgramContext(classOf[ListAptRepos], classOf[List[Repository]]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val repository: Repository = AptRepository(isSrc = true, "test", "test", List(), List())
      val repositories = List(repository)
      makeDigestReturn(repositories)

      // when
      val result = service.obtainRepositories

      // then
      result must beSome(repositories).await
    } tag UnitTest

    "add repositories" in new ProgramContext(classOf[AptAddRepository], classOf[Boolean]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val repository = AptRepository(isSrc = true, "test", "test", List(), List())
      val repositories = List(repository)
      makeDigestReturn(true)

      // when
      val result = service addRepositories repositories

      // then
      result must beSome(true).await
    } tag UnitTest

    "remove repositories" in new ProgramContext(classOf[AptRemoveRepository], classOf[Boolean]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val repository = AptRepository(isSrc = true, "test", "test", List(), List())
      val repositories = List(repository)
      makeDigestReturn(true)

      // when
      val result = service removeRepositories repositories

      // then
      result must beSome(true).await
    } tag UnitTest

    "update repositories" in new ProgramContext(classOf[AptGetUpdate], classOf[Boolean]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      makeDigestReturn(true)

      // when
      val result = service.updateRepositories

      // then
      result must beSome(true).await
    } tag UnitTest

    "install packages" in new ProgramContext(classOf[AptGetInstall], classOf[Boolean]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val package_ = NonVersionedPackage("test")
      val packages = List(package_)
      makeDigestReturn(true)

      // when
      val result = service installAll packages

      // then
      result must beSome(true).await
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
      result must beSome(true).await
    } tag UnitTest
  }
}
