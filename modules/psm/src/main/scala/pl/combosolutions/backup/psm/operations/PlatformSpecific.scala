package pl.combosolutions.backup.psm.operations

import java.nio.file.Path

import pl.combosolutions.backup.{ AsyncResult, Logging, ReportException }
import pl.combosolutions.backup.psm.OperatingSystem
import pl.combosolutions.backup.psm.PsmExceptionMessages._
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, ObligatoryElevationMode }
import pl.combosolutions.backup.psm.elevation.posix.SudoElevation
import pl.combosolutions.backup.psm.elevation.posix.linux.{ KDESudoElevation, GKSudoElevation }
import pl.combosolutions.backup.psm.elevation.windows.{ EmptyElevation, UACElevation }
import pl.combosolutions.backup.psm.filesystem.posix.PosixFileSystem
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.psm.repositories.posix.linux.AptRepositories

object PlatformSpecific {

  lazy val current: PlatformSpecific = new CalculatedPlatformSpecific(
    currentElevation,
    currentFileSystem,
    currentRepositories
  )

  private lazy val currentElevation = List(
    // Windows elevation
    EmptyElevation,
    UACElevation,

    // Linux elevation
    GKSudoElevation,
    KDESudoElevation,

    // POSIX elevation
    SudoElevation
  ) find (_.elevationAvailable) getOrElse (ReportException onIllegalStateOf NoElevationAvailable)

  private lazy val currentFileSystem = List(
    // POSIX file system
    PosixFileSystem
  ) find (_.fileSystemAvailable) getOrElse (ReportException onIllegalStateOf NoFileSystemAvailable)

  private lazy val currentRepositories = List(
    // Linux repositories
    AptRepositories
  ) find (_.repositoriesAvailable) getOrElse (ReportException onIllegalStateOf NoRepositoriesAvailable)
}

trait PlatformSpecific
  extends PlatformSpecificElevation
  with PlatformSpecificFileSystem
  with PlatformSpecificRepositories

class CalculatedPlatformSpecific(
  elevationPS: PlatformSpecificElevation,
  fileSystemPS: PlatformSpecificFileSystem,
  repositoriesPS: PlatformSpecificRepositories)
    extends PlatformSpecific with Logging {

  logger trace s"Operating System -> ${OperatingSystem.current.name}"
  logger trace s"Elevation        -> ${elevationPS.getClass.getSimpleName}"
  logger trace s"File System      -> ${fileSystemPS.getClass.getSimpleName}"
  logger trace s"Repositories     -> ${repositoriesPS.getClass.getSimpleName}"

  // elevation

  override val elevationAvailable = elevationPS.elevationAvailable
  override val elevationCMD = elevationPS.elevationCMD
  override val elevationArgs = elevationPS.elevationArgs

  override def elevateDirect[T <: Program[T]](program: Program[T]) = elevationPS elevateDirect program
  override def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner) =
    elevationPS elevateRemote (program, cleaner)

  // file system

  override val fileSystemAvailable = fileSystemPS.fileSystemAvailable
  override val fileIsFile = fileSystemPS.fileIsFile
  override val fileIsDirectory = fileSystemPS.fileIsDirectory
  override val fileIsSymlinkPattern = fileSystemPS.fileIsSymlinkPattern

  override def getFileType(forPath: Path)(implicit withElevation: ElevationMode, cleaner: Cleaner) = fileSystemPS getFileType forPath

  // repositories

  override val repositoriesAvailable = repositoriesPS.repositoriesAvailable

  // format: OFF
  override def obtainRepositories(implicit withElevation: ElevationMode, cleaner: Cleaner): AsyncResult[Repositories] =
    repositoriesPS obtainRepositories
  override def addRepositories(repositories: Repositories)
                              (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
    repositoriesPS addRepositories repositories
  override def removeRepositories(repositories: Repositories)
                                 (implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
    repositoriesPS removeRepositories repositories
  // format: ON

  override def areAllInstalled(packages: Packages)(implicit withElevation: ElevationMode, cleaner: Cleaner) =
    repositoriesPS areAllInstalled packages
  override def installAll(packages: Packages)(implicit withElevation: ObligatoryElevationMode, cleaner: Cleaner) =
    repositoriesPS installAll packages
}
