package pl.combosolutions.backup.psm.programs.posix

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.PsmExceptionMessages.UnknownFileType
import pl.combosolutions.backup.psm.filesystem.{ FileSystemServiceComponent, FileType }
import FileType.FileType
import pl.combosolutions.backup.psm.programs.{ Program, Result }

object PosixPrograms extends ComponentsHelper {
  this: FileSystemServiceComponent =>

  val fileIsDirectory = fileSystemService.fileIsDirectory
  val fileIsSymlinkPattern = fileSystemService.fileIsSymlinkPattern
  val fileIsFile = fileSystemService.fileIsFile

  type CatFileInterpreter[U] = Result[CatFile]#Interpreter[U]
  implicit val CatFile2Content: CatFileInterpreter[List[String]] = _.stdout

  type ListFileInterpreter[U] = Result[FileInfo]#Interpreter[U]
  implicit val FileInfo2FileType: ListFileInterpreter[FileType] = _.stdout.headOption map {
    case fileSystemService.fileIsDirectory(fileName) => FileType.Directory
    case fileSystemService.fileIsSymlinkPattern(fileName) => FileType.SymbolicLink
    case fileSystemService.fileIsFile(fileName) => FileType.File
  } getOrElse (ReportException onIllegalStateOf UnknownFileType)

  type GrepFilesInterpreter[U] = Result[GrepFiles]#Interpreter[U]
  implicit val GrepFiles2ListString: ListFileInterpreter[List[String]] = _.stdout

  type WhichProgramInterpreter[U] = Result[WhichProgram]#Interpreter[U]
  implicit val WhichProgram2Boolean: WhichProgramInterpreter[Boolean] = _.exitValue == 0
}

case class CatFile(fileName: String) extends Program[CatFile]("cat", List(fileName))

case class FileInfo(fileName: String) extends Program[FileInfo]("file", List(fileName))

case class GrepFiles(pattern: String, files: List[String]) extends Program[GrepFiles]("grep", "-h" :: pattern :: files)

case class WhichProgram(program: String) extends Program[WhichProgram]("which", List(program))
