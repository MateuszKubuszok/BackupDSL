package pl.combosolutions.backup.dsl.internals.operations.posix.linux

import pl.combosolutions.backup.dsl.internals.operations.PlatformSpecificRepositories
import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult
import pl.combosolutions.backup.dsl.internals.operations.posix.PosixPrograms._
import pl.combosolutions.backup.dsl.internals.operations.posix.WhichProgram
import pl.combosolutions.backup.dsl.internals.repositories.{Repository, VersionedPackage, AptRepository}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import scalaz.OptionT._
import scalaz.std.scalaFuture._

import AptPrograms._

object AptRepositories extends PlatformSpecificRepositories {

  lazy val aptSourcePattern  = "(deb|deb-src)\\s+(\\[arch=(\\S+)\\]\\s+)?(\\S+)\\s+(\\S+)((\\s+\\S+)*)".r
  lazy val etcAptSourcesMain = "/etc/apt/sources.list"
  lazy val etcAptSourcesDir  = "/etc/apt/sources.list.d/*"
  lazy val installedPattern  = "^ii\\s+(\\S+)\\s+(\\S+)".r

  override lazy val repositoriesAvailable: Boolean =
    Await.result(WhichProgram("apt-get").digest[Boolean], Duration.Inf) getOrElse false

  override def obtainRepositories: AsyncResult[List[Repository]] = ListAptRepos.digest[List[AptRepository]]

  override def addRepositories(repositories: Repositories): AsyncResult[Boolean] =
    areAllTrueWithinAsyncResults(asApt(repositories) map (AptAddRepository(_).digest[Boolean]))

  override def removeRepositories(repositories: Repositories): AsyncResult[Boolean] =
    areAllTrueWithinAsyncResults(asApt(repositories) map (AptRemoveRepository(_).digest[Boolean]))

  override def installAll(packages: Packages): AsyncResult[Boolean] = AptGetInstall(packages toList).digest[Boolean]

  override def areAllInstalled(packages: Packages): AsyncResult[Boolean] = (for {
    installedPackages <- optionT[Future](DpkgList.digest[List[VersionedPackage]])
  } yield packages.forall(package_ => installedPackages.exists(iPackage => iPackage.name == package_.name))).run

  private def asApt(list: Repositories) = list collect { case ar: AptRepository => ar }

  private def areAllTrueWithinAsyncResults(futureOptBooleans: List[AsyncResult[Boolean]]): AsyncResult[Boolean] =
    Future sequence futureOptBooleans map { resultOpts =>
      if   (resultOpts exists (_.isEmpty)) None
      else Some(resultOpts collect { case Some(boolean) => boolean } forall identity)
    }
}
