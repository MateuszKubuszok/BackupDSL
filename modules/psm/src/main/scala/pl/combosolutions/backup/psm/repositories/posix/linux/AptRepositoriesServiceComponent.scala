package pl.combosolutions.backup.psm.repositories.posix.linux

import pl.combosolutions.backup.{ Cleaner, ExecutionContexts, Async, AsyncTransformer }
import ExecutionContexts.Task.context
import pl.combosolutions.backup.psm
import psm.elevation.{ ElevateIfNeeded, ElevationMode, ObligatoryElevationMode }
import ElevateIfNeeded._
import psm.programs.posix.{ PosixPrograms, WhichProgram }
import PosixPrograms._
import psm.programs.posix.linux._
import AptPrograms._
import DpkgPrograms._
import psm.repositories.{ AptRepository, RepositoriesService, RepositoriesServiceComponent, VersionedPackage }

import scala.concurrent.duration.Duration
import scala.concurrent.Await

trait AptRepositoriesServiceComponent extends RepositoriesServiceComponent {

  override def repositoriesService: RepositoriesService = AptRepositoriesService

  trait AptRepositoriesService extends RepositoriesService {

    override lazy val repositoriesAvailable: Boolean =
      Await.result(WhichProgram("apt-get").digest[Boolean], Duration.Inf) getOrElse false

    override def obtainRepositories(implicit withElevation: ElevationMode, cleaner: Cleaner): Async[Repositories] =
      ListAptRepos.handleElevation.digest[List[AptRepository]]

    // format: OFF
    override def addRepositories(repositories: Repositories)
                                (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): Async[Boolean] =
      areAllTrueWithinAsyncs(asApt(repositories) map (AptAddRepository(_).handleElevation.digest[Boolean]))

    override def removeRepositories(repositories: Repositories)
                                   (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): Async[Boolean] =
      areAllTrueWithinAsyncs(asApt(repositories) map (AptRemoveRepository(_).handleElevation.digest[Boolean]))

    override def updateRepositories(implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): Async[Boolean] =
      AptGetUpdate.handleElevation.digest[Boolean]

    override def installAll(packages: Packages)
                           (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner): Async[Boolean] =
      AptGetInstall(packages toList).handleElevation.digest[Boolean]

    override def areAllInstalled(packages: Packages)
                                (implicit withElevation: ElevationMode, cleaner: Cleaner): Async[Boolean] =
      DpkgList.handleElevation.digest[List[VersionedPackage]].asAsync map { installedPackages =>
        packages forall (package_ => installedPackages.exists(iPackage => iPackage.name == package_.name))
      }
    // format: ON

    private def asApt(list: Repositories) = list collect { case ar: AptRepository => ar }

    private def areAllTrueWithinAsyncs(futureOptBooleans: List[Async[Boolean]]): Async[Boolean] =
      (Async completeSequence futureOptBooleans).asAsync map (_ forall identity)
  }

  object AptRepositoriesService extends AptRepositoriesService
}

object AptRepositoriesServiceComponent extends AptRepositoriesServiceComponent {

  lazy val aptSourcePattern = "(deb|deb-src)\\s+(\\[arch=(\\S+)\\]\\s+)?(\\S+)\\s+(\\S+)((\\s+\\S+)*)".r
  lazy val etcAptSourcesMain = "/etc/apt/sources.list"
  lazy val etcAptSourcesDir = "/etc/apt/sources.list.d/*"
  lazy val installedPattern = "^ii\\s+(\\S+)\\s+(\\S+)".r
}
