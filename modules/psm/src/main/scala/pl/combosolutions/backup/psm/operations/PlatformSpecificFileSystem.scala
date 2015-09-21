package pl.combosolutions.backup.psm.operations

import java.nio.file.Path

import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.psm.elevation.ElevationMode
import pl.combosolutions.backup.psm.filesystem.FileType
import FileType._

import scala.util.matching.Regex

trait PlatformSpecificFileSystem {
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
