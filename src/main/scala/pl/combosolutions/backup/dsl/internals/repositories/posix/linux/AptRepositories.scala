package pl.combosolutions.backup.dsl.internals.repositories.posix.linux

import pl.combosolutions.backup.dsl.internals.elevation.{ObligatoryElevationMode, ElevationMode}
import pl.combosolutions.backup.dsl.internals.elevation.ElevateIfNeeded._
import pl.combosolutions.backup.dsl.internals.operations.{Cleaner, PlatformSpecificRepositories}
import pl.combosolutions.backup.dsl.internals.programs.Program
import Program.AsyncResult
import pl.combosolutions.backup.dsl.internals.programs.posix.{WhichProgram, PosixPrograms}
import PosixPrograms._
import pl.combosolutions.backup.dsl.internals.programs.posix.linux._
import AptPrograms._
import pl.combosolutions.backup.dsl.internals.repositories.{AptRepository, VersionedPackage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scalaz.OptionT._
import scalaz.std.scalaFuture._

object AptRepositories extends PlatformSpecificRepositories {

  lazy val aptSourcePattern  = "(deb|deb-src)\\s+(\\[arch=(\\S+)\\]\\s+)?(\\S+)\\s+(\\S+)((\\s+\\S+)*)".r
  lazy val etcAptSourcesMain = "/etc/apt/sources.list"
  lazy val etcAptSourcesDir  = "/etc/apt/sources.list.d/*"
  lazy val installedPattern  = "^ii\\s+(\\S+)\\s+(\\S+)".r

  override lazy val repositoriesAvailable: Boolean =
    Await.result(WhichProgram("apt-get").digest[Boolean], Duration.Inf) getOrElse false

  override def obtainRepositories(implicit withElevation: ElevationMode, cleaner: Cleaner) =
    ListAptRepos.handleElevation.digest[List[AptRepository]]

  override def addRepositories(repositories: Repositories)(implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
    areAllTrueWithinAsyncResults(asApt(repositories) map (AptAddRepository(_).handleElevation.digest[Boolean]))

  override def removeRepositories(repositories: Repositories)(implicit  withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
    areAllTrueWithinAsyncResults(asApt(repositories) map (AptRemoveRepository(_).handleElevation.digest[Boolean]))

  override def installAll(packages: Packages)(implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
    AptGetInstall(packages toList).handleElevation.digest[Boolean]

  override def areAllInstalled(packages: Packages)(implicit withElevation: ElevationMode, cleaner: Cleaner) = (for {
    installedPackages <- optionT[Future](DpkgList.handleElevation.digest[List[VersionedPackage]])
  } yield packages.forall(package_ => installedPackages.exists(iPackage => iPackage.name == package_.name))).run

  private def asApt(list: Repositories) = list collect { case ar: AptRepository => ar }

  private def areAllTrueWithinAsyncResults(futureOptBooleans: List[AsyncResult[Boolean]]): AsyncResult[Boolean] =
    Future sequence futureOptBooleans map { resultOpts =>
      if   (resultOpts exists (_.isEmpty)) None
      else Some(resultOpts collect { case Some(boolean) => boolean } forall identity)
    }
}
