package pl.combosolutions.backup.psm.filesystem

import java.io.File
import java.nio.file.Path

import pl.combosolutions.backup.{ Async, Cleaner, ExecutionContexts, AsyncTransformer }
import ExecutionContexts.Command.context
import pl.combosolutions.backup.psm.commands.CommonCommands._
import pl.combosolutions.backup.psm.commands.{ CopyCommand, DeleteCommand, MoveCommand }
import pl.combosolutions.backup.psm.elevation.ElevationMode
import pl.combosolutions.backup.psm.elevation.ElevateIfNeeded._

trait CommonFileSystemServiceComponent extends FileSystemServiceComponent {
  self: FileSystemServiceComponent =>

  trait CommonFileSystemService extends FileSystemService {

    // format: OFF
    override def copyFiles(files: List[(Path, Path)])
                          (implicit withElevation: ElevationMode, cleaner: Cleaner): Async[List[Path]] =
      CopyCommand(paths2Strings(files)).handleElevation.digest[List[String]].asAsync map string2Path

    override def deleteFiles(files: List[Path])
                            (implicit withElevation: ElevationMode, cleaner: Cleaner): Async[List[Path]] =
      DeleteCommand(path2String(files)).handleElevation.digest[List[String]].asAsync map string2Path

    override def moveFiles(files: List[(Path, Path)])
                          (implicit withElevation: ElevationMode, cleaner: Cleaner): Async[List[Path]] =
      MoveCommand(paths2Strings(files)).handleElevation.digest[List[String]].asAsync map string2Path
    // format: ON

    protected def path2String(files: List[Path]): List[String] = files map (_.toString)

    protected def paths2Strings(files: List[(Path, Path)]): List[(String, String)] =
      files map (paths => (paths._1.toString, paths._2.toString))

    protected def string2Path(files: List[String]): List[Path] = files map (new File(_).toPath)
  }
}
