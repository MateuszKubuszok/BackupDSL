package pl.combosolutions.backup.psm.repositories.posix.linux

import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.wrapAsyncResultForMapping
import pl.combosolutions.backup.psm.ExecutionContexts.Task.context
import pl.combosolutions.backup.psm.elevation.{ ElevateIfNeeded, ElevationMode, ObligatoryElevationMode }
import ElevateIfNeeded._
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.posix.{ PosixPrograms, WhichProgram }
import PosixPrograms._
import pl.combosolutions.backup.psm.programs.posix.linux._
import AptPrograms._
import pl.combosolutions.backup.psm.repositories.{ AptRepository, RepositoriesService, RepositoriesServiceComponent, VersionedPackage }

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import scalaz.OptionT._
import scalaz.std.scalaFuture._

trait AptRepositoriesServiceComponent extends RepositoriesServiceComponent {

  override def repositoriesService: RepositoriesService = AptRepositoriesService

  trait AptRepositoriesService extends RepositoriesService {

    override lazy val repositoriesAvailable: Boolean =
      Await.result(WhichProgram("apt-get").digest[Boolean], Duration.Inf) getOrElse false

    override def obtainRepositories(implicit withElevation: ElevationMode, cleaner: Cleaner) =
      ListAptRepos.handleElevation.digest[List[AptRepository]]

    // format: OFF
    override def addRepositories(repositories: Repositories)
                                (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
      areAllTrueWithinAsyncResults(asApt(repositories) map (AptAddRepository(_).handleElevation.digest[Boolean]))

    override def removeRepositories(repositories: Repositories)
                                   (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
      areAllTrueWithinAsyncResults(asApt(repositories) map (AptRemoveRepository(_).handleElevation.digest[Boolean]))

    override def installAll(packages: Packages)
                           (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
      AptGetInstall(packages toList).handleElevation.digest[Boolean]

    override def areAllInstalled(packages: Packages)
                                (implicit withElevation: ElevationMode, cleaner: Cleaner) = (for {
      installedPackages <- optionT[Future](DpkgList.handleElevation.digest[List[VersionedPackage]])
    } yield packages.forall(package_ => installedPackages.exists(iPackage => iPackage.name == package_.name))).run
    // format: ON

    private def asApt(list: Repositories) = list collect { case ar: AptRepository => ar }

    private def areAllTrueWithinAsyncResults(futureOptBooleans: List[AsyncResult[Boolean]]): AsyncResult[Boolean] =
      (AsyncResult completeSequence futureOptBooleans).asAsync map (_ forall identity)
  }

  object AptRepositoriesService extends AptRepositoriesService
}

object AptRepositoriesServiceComponent extends AptRepositoriesServiceComponent {

  lazy val aptSourcePattern = "(deb|deb-src)\\s+(\\[arch=(\\S+)\\]\\s+)?(\\S+)\\s+(\\S+)((\\s+\\S+)*)".r
  lazy val etcAptSourcesMain = "/etc/apt/sources.list"
  lazy val etcAptSourcesDir = "/etc/apt/sources.list.d/*"
  lazy val installedPattern = "^ii\\s+(\\S+)\\s+(\\S+)".r
}
