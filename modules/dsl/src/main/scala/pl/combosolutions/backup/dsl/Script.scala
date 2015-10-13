package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.Logging
import pl.combosolutions.backup.dsl.oldtasks.{ BackupFiles, RootTask, Task }
import pl.combosolutions.backup.psm.elevation.ElevationServiceComponent
import pl.combosolutions.backup.psm.{ ComponentsHelper, ExecutionContexts }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.tasks.Action._
import pl.combosolutions.backup.tasks.{ Action, Settings }

import scala.concurrent.ExecutionContext

abstract class Script(name: String) extends Cleaner with Logging with ComponentsHelper {
  self: Cleaner with Logging with ElevationServiceComponent =>

  implicit val context: ExecutionContext = ExecutionContexts.Task.context

  private val parser = new scopt.OptionParser[ScriptConfig](name) {
    head("backup/restore script made with BackupDSL")

    (arg[Action]("action")
      action { (action, conf) => conf.copy(action = action) }
      text "whether to perform backup or restore"
      required ())

    (help("help")
      text "displays help")

    // pass variables into the Script: var=val
  }

  private val rootTask = new RootTask

  implicit val defaultSettings = Settings

  final def addTask[BR, RR](task: Task[Unit, Unit, BR, RR]): Task[Unit, Unit, BR, RR] = rootTask andThen task

  protected def elevate[T <: Program[T]](program: Program[T]): Program[T] =
    elevationService elevateRemote (program, this)

  private final def execute(config: ScriptConfig): Unit = config.action match {
    case Action.Backup =>
      logger info s"Running BACKUP: $name"
      logger trace s"with configuration $config"
      backup
    case Action.Restore =>
      logger info s"Running RESTORE: $name"
      logger trace s"with configuration $config"
      restore
  }

  private final def backup = rootTask performBackup

  private final def restore = rootTask performRestore

  def main(args: Array[String]): Unit = {
    parser.parse(args, ScriptConfig()) foreach execute
    clean
  }

  def backupFiles[PBR, PRR](files: String*) = BackupFiles[PBR, PRR](files toList)
}
