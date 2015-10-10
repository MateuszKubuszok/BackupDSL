package pl.combosolutions.backup.psm.commands

import java.io.File

import pl.combosolutions.backup.psm.filesystem.FilesServiceComponentImpl
import pl.combosolutions.backup.{ Async, Result }
import pl.combosolutions.backup.psm.ExecutionContexts.Command.context

object CommonCommands {

  type CopyCommandInterpreter[U] = Result[CopyCommand]#Interpreter[U]
  implicit val CopyCommand2Boolean: CopyCommandInterpreter[Boolean] = _.exitValue == 0
  implicit val CopyCommand2Tuple: CopyCommandInterpreter[(List[String], List[String])] = r => (r.stdout, r.stderr)

  type DeleteCommandInterpreter[U] = Result[DeleteCommand]#Interpreter[U]
  implicit val DeleteCommand2Boolean: DeleteCommandInterpreter[Boolean] = _.exitValue == 0
  implicit val DeleteCommand2Tuple: DeleteCommandInterpreter[(List[String], List[String])] = r => (r.stdout, r.stderr)

  type MoveCommandInterpreter[U] = Result[MoveCommand]#Interpreter[U]
  implicit val MoveCommand2Boolean: MoveCommandInterpreter[Boolean] = _.exitValue == 0
  implicit val MoveCommand2Tuple: MoveCommandInterpreter[(List[String], List[String])] = r => (r.stdout, r.stderr)
}

private[commands] abstract class FilesCommand[T <: Command[T]]
    extends Command[T]
    with FilesServiceComponentImpl {

  protected def filesOperation: List[(String, Boolean)]

  override def run: Async[Result[T]] = Async {
    val parsedResults = filesOperation groupBy (_._2) mapValues (_.map(_._1))
    val succeedFiles = parsedResults.getOrElse(true, List())
    val failedFiles = parsedResults.getOrElse(false, List())
    Some(Result(failedFiles.size, succeedFiles, failedFiles))
  }
}

case class CopyCommand(files: List[(String, String)]) extends FilesCommand[CopyCommand] {

  override def filesOperation = for {
    (fromFileName, intoFileName) <- files
    fromFile = new File(fromFileName).getAbsoluteFile.toPath
    intoFile = new File(intoFileName).getAbsoluteFile.toPath
  } yield (fromFileName, filesService copy (fromFile, intoFile))
}

case class DeleteCommand(files: List[String]) extends FilesCommand[DeleteCommand] {

  override def filesOperation = for {
    fileName <- files
    file = new File(fileName).getAbsoluteFile.toPath
  } yield (fileName, filesService delete file)
}

case class MoveCommand(files: List[(String, String)]) extends FilesCommand[MoveCommand] {

  override def filesOperation = for {
    (fromFileName, intoFileName) <- files
    fromFile = new File(fromFileName).getAbsoluteFile.toPath
    intoFile = new File(intoFileName).getAbsoluteFile.toPath
  } yield (fromFileName, filesService move (fromFile, intoFile))
}
