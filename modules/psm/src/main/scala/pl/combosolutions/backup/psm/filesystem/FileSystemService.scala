package pl.combosolutions.backup.psm.filesystem

import java.nio.file.Path

import pl.combosolutions.backup.{ Cleaner, Async }
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.ImplementationResolver
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoFileSystemAvailable
import pl.combosolutions.backup.psm.elevation.ElevationMode
import pl.combosolutions.backup.psm.filesystem.FileType._
import pl.combosolutions.backup.psm.filesystem.posix.PosixFileSystemServiceComponent

import FileSystemServiceComponentImpl.resolve

trait FileSystemService {

  val fileSystemAvailable: Boolean

  val fileSystemPriority: ImplementationPriority

  def getFileType(forPath: Path)(implicit withElevation: ElevationMode, cleaner: Cleaner): Async[FileType]

  def isSupportingSymbolicLinks: Boolean

  def linkFiles(files: List[(Path, Path)])(implicit withElevation: ElevationMode, cleaner: Cleaner): Async[List[Path]]

  def copyFiles(files: List[(Path, Path)])(implicit withElevation: ElevationMode, cleaner: Cleaner): Async[List[Path]]

  def deleteFiles(files: List[Path])(implicit withElevation: ElevationMode, cleaner: Cleaner): Async[List[Path]]

  def moveFiles(files: List[(Path, Path)])(implicit withElevation: ElevationMode, cleaner: Cleaner): Async[List[Path]]
}

trait FileSystemServiceComponent {

  def fileSystemService: FileSystemService
}

// $COVERAGE-OFF$ Implementation resolution should be checked on each implementation level
object FileSystemServiceComponentImpl extends ImplementationResolver[FileSystemService] {

  override lazy val implementations = Seq(
    // POSIX file system
    PosixFileSystemServiceComponent.fileSystemService
  )

  override lazy val notFoundMessage = NoFileSystemAvailable

  override def byFilter(service: FileSystemService): Boolean = service.fileSystemAvailable

  override def byPriority(service: FileSystemService): ImplementationPriority = service.fileSystemPriority
}

trait FileSystemServiceComponentImpl extends FileSystemServiceComponent {

  override lazy val fileSystemService = resolve
}
// $COVERAGE-ON$
