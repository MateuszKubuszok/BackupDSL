package pl.combosolutions.backup.psm.filesystem

import java.nio.file.Path

import pl.combosolutions.backup.Async
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.ImplementationResolver
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoFileSystemAvailable
import pl.combosolutions.backup.psm.elevation.ElevationMode
import pl.combosolutions.backup.psm.filesystem.FileType._
import pl.combosolutions.backup.psm.filesystem.posix.PosixFileSystemServiceComponent
import pl.combosolutions.backup.psm.operations.Cleaner

import FileSystemServiceComponentImpl.resolve

trait FileSystemService {

  val fileSystemAvailable: Boolean

  def getFileType(forPath: Path)(implicit withElevation: ElevationMode, cleaner: Cleaner): Async[FileType]

  def isSupportingSymbolicLinks: Boolean

  /* // TODO
  def createSymlink(from: Path, to: Path, withElevation: Boolean) = throw new NotImplementedError("TODO")

  def copyFiles(files: List[Path], withElevation: Boolean) = throw new NotImplementedError("TODO")

  def deleteFiles(files: List[Path], withElevation: Boolean) = throw new NotImplementedError("TODO")

  def moveFiles(files: List[Path], withElevation: Boolean) = throw new NotImplementedError("TODO")
  */
}

trait FileSystemServiceComponent {

  def fileSystemService: FileSystemService
}

object FileSystemServiceComponentImpl extends ImplementationResolver[FileSystemService] {

  override lazy val implementations = Seq(
    // POSIX file system
    PosixFileSystemServiceComponent.fileSystemService
  )

  override lazy val notFoundMessage = NoFileSystemAvailable

  override def byFilter(service: FileSystemService): Boolean = service.fileSystemAvailable

  // TODO: improve
  override def byPriority(service: FileSystemService): ImplementationPriority =
    if (service.fileSystemAvailable) Allowed
    else NotAllowed
}

trait FileSystemServiceComponentImpl extends FileSystemServiceComponent {

  override lazy val fileSystemService = resolve
}
