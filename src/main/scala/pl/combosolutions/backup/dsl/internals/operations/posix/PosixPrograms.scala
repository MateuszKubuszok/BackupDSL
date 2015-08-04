package pl.combosolutions.backup.dsl.internals.operations.posix

import pl.combosolutions.backup.dsl.internals.filesystem.FileType
import pl.combosolutions.backup.dsl.internals.filesystem.FileType.FileType
import pl.combosolutions.backup.dsl.internals.operations.{PlatformSpecific, Result, Program}

object PosixPrograms {
  type CatFileInterpreter[U] = Result[CatFile]#Interpreter[U]
  implicit val CatFile2Content: CatFileInterpreter[List[String]] = _.stdout

  type ListFileInterpreter[U] = Result[FileInfo]#Interpreter[U]
  implicit val FileInfo2FileType: ListFileInterpreter[FileType] = _.stdout.headOption map {
    case PlatformSpecific.current.fileIsDirectory(fileName)      => FileType.Directory
    case PlatformSpecific.current.fileIsSymlinkPattern(fileName) => FileType.SymbolicLink
    case PlatformSpecific.current.fileIsFile(fileName)           => FileType.File
  } getOrElse (throw new InternalError("Unexpected `file` answer"))
}

case class CatFile(fileName: String) extends Program[CatFile]("cat", List(fileName))

case class FileInfo(fileName: String) extends Program[FileInfo]("file", List(fileName))

case class GrepFiles(pattern: String, files: List[String]) extends Program[GrepFiles]("grep", "-h" :: pattern :: files)
