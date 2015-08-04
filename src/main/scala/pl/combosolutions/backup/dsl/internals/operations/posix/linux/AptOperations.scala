package pl.combosolutions.backup.dsl.internals.operations.posix.linux

import pl.combosolutions.backup.dsl.internals.operations.PlatformSpecific
import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult
import pl.combosolutions.backup.dsl.internals.repositories.{VersionedPackage, AptRepository}

import scala.concurrent.Future

import scalaz.OptionT._

import AptPrograms._

object AptOperations {
  lazy val aptSourcePattern  = "(deb|deb-src)[ \t]+(\\w+)([ \t]+\\w+)".r
  lazy val etcAptSourcesMain = "/etc/apt/sources.list"
  lazy val etcAptSourcesDir  = "/etc/apt/sources.list.d/*"
  lazy val installedPattern  = "^ii\\s+(\\S+)\\s+(\\S+)".r
}

abstract trait AptOperations extends PlatformSpecific {

  override def obtainRepositories: AsyncResult[List[AptRepository]] = ListAptRepos.digest[List[AptRepository]]

  override def addRepositories(repositories: Repositories): AsyncResult[Boolean] =
    areAllTrueWithinAsyncResults(asApt(repositories) map (AptAddRepository(_).digest[Boolean]))

  override def removeRepositories(repositories: Repositories): AsyncResult[Boolean] =
    areAllTrueWithinAsyncResults(asApt(repositories) map (AptRemoveRepository(_).digest[Boolean]))

  override def installAll(packages: Packages): AsyncResult[Boolean] = AptGetInstall(packages toList).digest[Boolean]

  override def areAllInstalled(packages: Packages): AsyncResult[Boolean] = (for {
    installedPackages <- optionT[Future](DpkgList.digest[List[VersionedPackage]])
  } yield packages.forall(package_ => installedPackages.exists(iPackage => iPackage.name == package_.name))).run

  private def asApt(list: Repositories) = list collect { case ar: AptRepository => ar }

  private def areAllTrueWithinAsyncResults(futureOptBooleans: Traversable[AsyncResult[Boolean]]): AsyncResult[Boolean] =
    Future sequence futureOptBooleans map { resultOpts =>
      if   (resultOpts exists (_.isEmpty)) None
      else Some(resultOpts map { case Some(boolean) => boolean } forall identity)
    }
}
