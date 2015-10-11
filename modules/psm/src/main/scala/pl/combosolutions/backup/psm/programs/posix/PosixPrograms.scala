package pl.combosolutions.backup.psm.programs.posix

import pl.combosolutions.backup.{ ReportException, Result }
import pl.combosolutions.backup.psm.PsmExceptionMessages.UnknownFileType
import pl.combosolutions.backup.psm.filesystem.FileType.{ Directory, File, FileType, SymbolicLink }
import pl.combosolutions.backup.psm.programs.Program

object PosixPrograms {

  val fileIsFile = "(.*): .*".r
  val fileIsDirectory = "(.*): directory".r
  val fileIsSymlink = "(.*): symbolic link to .*".r

  type CatFileInterpreter[U] = Result[CatFile]#Interpreter[U]
  implicit val CatFile2Content: CatFileInterpreter[List[String]] = _.stdout

  type FileInfoInterpreter[U] = Result[FileInfo]#Interpreter[U]
  implicit val FileInfo2FileType: FileInfoInterpreter[FileType] = _.stdout.headOption map {
    case fileIsDirectory(fileName) => Directory
    case fileIsSymlink(fileName) => SymbolicLink
    case fileIsFile(fileName) => File
  } getOrElse (ReportException onIllegalStateOf UnknownFileType)

  type LinkFileInterpreter[U] = Result[LinkFile]#Interpreter[U]
  implicit val LinkFile2Boolean: LinkFileInterpreter[Boolean] = _.exitValue == 0

  type GrepFilesInterpreter[U] = Result[GrepFiles]#Interpreter[U]
  implicit val GrepFiles2ListString: GrepFilesInterpreter[List[String]] = _.stdout

  type WhichProgramInterpreter[U] = Result[WhichProgram]#Interpreter[U]
  implicit val WhichProgram2Boolean: WhichProgramInterpreter[Boolean] = _.exitValue == 0
}

case class CatFile(fileName: String) extends Program[CatFile]("cat", List(fileName))

case class FileInfo(fileName: String) extends Program[FileInfo]("file", List(fileName))

case class LinkFile(fromFile: String, toFile: String) extends Program[LinkFile]("ln", List("-s", fromFile, toFile))

case class GrepFiles(pattern: String, files: List[String]) extends Program[GrepFiles]("grep", "-h" :: pattern :: files)

case class WhichProgram(program: String) extends Program[WhichProgram]("which", List(program))
