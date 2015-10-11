package pl.combosolutions.backup.psm.filesystem

import java.io.File
import java.nio.file.Path

import pl.combosolutions.backup.wrapAsyncForMapping
import pl.combosolutions.backup.psm.ExecutionContexts.Command.context
import pl.combosolutions.backup.psm.commands.CommonCommands._
import pl.combosolutions.backup.psm.commands.{ CopyCommand, DeleteCommand, MoveCommand }
import pl.combosolutions.backup.psm.elevation.ElevationMode
import pl.combosolutions.backup.psm.elevation.ElevateIfNeeded._
import pl.combosolutions.backup.psm.operations.Cleaner

trait CommonFileSystemServiceComponent extends FileSystemServiceComponent {
  self: FileSystemServiceComponent =>

  trait CommonFileSystemService extends FileSystemService {

    override def copyFiles(files: List[(Path, Path)])(implicit withElevation: ElevationMode, cleaner: Cleaner) =
      CopyCommand(paths2Strings(files)).handleElevation.digest[(List[String], List[String])].asAsync.map(strings => string2Path(strings._1))

    override def deleteFiles(files: List[Path])(implicit withElevation: ElevationMode, cleaner: Cleaner) =
      DeleteCommand(path2String(files)).handleElevation.digest[(List[String], List[String])].asAsync.map(strings => string2Path(strings._1))

    override def moveFiles(files: List[(Path, Path)])(implicit withElevation: ElevationMode, cleaner: Cleaner) =
      MoveCommand(paths2Strings(files)).handleElevation.digest[(List[String], List[String])].asAsync.map(strings => string2Path(strings._1))

    protected def path2String(files: List[Path]): List[String] =
      files map (_.toString)

    protected def paths2Strings(files: List[(Path, Path)]): List[(String, String)] =
      files map (paths => (paths._1.toString, paths._2.toString))

    protected def string2Path(files: List[String]): List[Path] =
      files map (new File(_).toPath)
  }
}
