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
}
