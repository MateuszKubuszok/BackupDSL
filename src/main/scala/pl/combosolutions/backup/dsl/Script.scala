package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.dsl.tasks.{Task, BackupFiles, RootTask}
import pl.combosolutions.backup.dsl.Action._

abstract class Script(name: String) extends Logging {

  private val parser = new scopt.OptionParser[ScriptConfig](name) {
    head("backup/restore script made with BackupDSL")

    (arg[Action]("action")
      action { (action, conf) => conf.copy(action = action) }
      text   "whether to perform backup or restore"
      required)

    (help("help")
      text "displays help")

    // pass variables into the Script: var=val
  }

  private val rootTask = new RootTask

  implicit val defaultSettings = Settings
  
  final def addTask[BR,RR](task: Task[Unit,Unit,BR,RR]): Task[Unit,Unit,BR,RR] = rootTask andThen task

  private final def execute(config: ScriptConfig): Unit = config.action match {
    case Action.Backup  => logger info  s"Running BACKUP: ${name}"
                           logger trace s"        with configuration ${config}"
                           backup
    case Action.Restore => logger info  s"Running RESTORE: ${name}"
                           logger trace s"        with configuration ${config}"
                           restore
  }
  
  private final def backup = rootTask performBackup
  
  private final def restore = rootTask performRestore

  def main(args: Array[String]): Unit = parser.parse(args, ScriptConfig()) foreach execute

  def backupFiles[PBR,PRR](files: String*) = BackupFiles[PBR,PRR](files toList)
}
