package pl.combosolutions.backup.psm.filesystem

import java.nio.file.Path

import pl.combosolutions.backup.psm.PsmExceptionMessages.NoFileSystemAvailable
import pl.combosolutions.backup.psm.elevation.ElevationMode
import pl.combosolutions.backup.psm.filesystem.FileType._
import pl.combosolutions.backup.psm.filesystem.posix.PosixFileSystemService
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.{ AsyncResult, Logging, ReportException }

import scala.util.matching.Regex

trait FileSystemService {

  val fileSystemAvailable: Boolean

  val fileIsFile: Regex
  val fileIsDirectory: Regex
  val fileIsSymlinkPattern: Regex
  def getFileType(forPath: Path)(implicit withElevation: ElevationMode, cleaner: Cleaner): AsyncResult[FileType]

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

trait FileSystemServiceComponentImpl extends FileSystemServiceComponent with Logging {

  override lazy val fileSystemService = Seq(
    // POSIX file system
    PosixFileSystemService
  ) find (_.fileSystemAvailable) getOrElse (ReportException onIllegalStateOf NoFileSystemAvailable)
}
