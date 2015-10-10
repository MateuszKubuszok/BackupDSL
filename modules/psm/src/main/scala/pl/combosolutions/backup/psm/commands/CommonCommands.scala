package pl.combosolutions.backup.psm.commands

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.{ ATOMIC_MOVE, COPY_ATTRIBUTES, REPLACE_EXISTING }

import pl.combosolutions.backup.{ Async, Result }
import pl.combosolutions.backup.psm.ExecutionContexts.Command.context

import scala.util.Try

class CommonCommands {

  type CopyCommandInterpreter[U] = Result[CopyCommand]#Interpreter[U]
  implicit val CopyCommand2Boolean: CopyCommandInterpreter[Boolean] = _.exitValue == 0
  implicit val CopyCommand2Tuple: CopyCommandInterpreter[(List[String], List[String])] = r => (r.stdout, r.stderr)
}

case class CopyCommand(files: List[(String, String)]) extends Command[CopyCommand] {

  override def run: Async[Result[CopyCommand]] = Async {
    val rawResults = for {
      (fromFileName, toFileName) <- files
      fromFile = new File(fromFileName).getAbsoluteFile.toPath
      toFile = new File(toFileName).getAbsoluteFile.toPath
    } yield (fromFileName, Try(Files.copy(fromFile, toFile, ATOMIC_MOVE, COPY_ATTRIBUTES, REPLACE_EXISTING)))

    val parsedResults = rawResults.groupBy(_._2.isSuccess).mapValues { values => values.map(_._1) }

    val copiedFiles = parsedResults(true)
    val failedFiles = parsedResults(false)

    Some(Result(failedFiles.size, copiedFiles, failedFiles))
  }
}
