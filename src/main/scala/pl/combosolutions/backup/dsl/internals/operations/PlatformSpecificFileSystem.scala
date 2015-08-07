package pl.combosolutions.backup.dsl.internals.operations

import java.nio.file.Path

import pl.combosolutions.backup.dsl.internals.filesystem.FileType._
import pl.combosolutions.backup.dsl.internals.operations.Program._

import scala.util.matching.Regex

trait PlatformSpecificFileSystem {
  val fileSystemAvailable: Boolean

  val fileIsFile: Regex
  val fileIsDirectory: Regex
  val fileIsSymlinkPattern: Regex
  def getFileType(path: Path): AsyncResult[FileType]

  /* // TODO
  def createSymlink(from: Path, to: Path, withElevation: Boolean) = throw new NotImplementedError("TODO")

  def copyFiles(files: List[Path], withElevation: Boolean) = throw new NotImplementedError("TODO")

  def deleteFiles(files: List[Path], withElevation: Boolean) = throw new NotImplementedError("TODO")

  def moveFiles(files: List[Path], withElevation: Boolean) = throw new NotImplementedError("TODO")
  */
}
