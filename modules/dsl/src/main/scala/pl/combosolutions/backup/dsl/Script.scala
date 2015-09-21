package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.Logging
import pl.combosolutions.backup.dsl.tasks.{ Task, BackupFiles, RootTask }
import pl.combosolutions.backup.dsl.Action._
import pl.combosolutions.backup.psm.ExecutionContexts
import pl.combosolutions.backup.psm.operations.{Cleaner, PlatformSpecific}
import pl.combosolutions.backup.psm.programs.Program

import scala.concurrent.ExecutionContext

abstract class Script(name: String) extends Logging with Cleaner {

  implicit val context: ExecutionContext = ExecutionContexts.Task.context

  private val parser = new scopt.OptionParser[ScriptConfig](name) {
    head("backup/restore script made with BackupDSL")

    (arg[Action]("action")
      action { (action, conf) => conf.copy(action = action) }
      text "whether to perform backup or restore" required)

    (help("help")
      text "displays help")

    // pass variables into the Script: var=val
  }

  private val rootTask = new RootTask

  implicit val defaultSettings = Settings

  final def addTask[BR, RR](task: Task[Unit, Unit, BR, RR]): Task[Unit, Unit, BR, RR] = rootTask andThen task

  protected def elevate[T <: Program[T]](program: Program[T]): Program[T] =
    PlatformSpecific.current.elevateRemote(program, this)

  private final def execute(config: ScriptConfig): Unit = config.action match {
    case Action.Backup =>
      logger info s"Running BACKUP: ${name}"
      logger trace s"with configuration ${config}"
      backup
    case Action.Restore =>
      logger info s"Running RESTORE: ${name}"
      logger trace s"with configuration ${config}"
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
