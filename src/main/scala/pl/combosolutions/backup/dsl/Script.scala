package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.dsl.tasks.{BackupFiles, RootTask}
import pl.combosolutions.backup.dsl.Action._

abstract class Script(name: String) {

  private val parser = new scopt.OptionParser[ScriptConfig](name) {
    head("backup/restore script made with BackupDSL")

    (arg[Action]("action")
      action { (action, conf) => conf.copy(action = action) }
      text   "whether to perform backup or restore"
      required)

    (help("help")
      text "displays help")
  }

  private val rootTask = new RootTask

  implicit val defaultSettings = Settings
  
  final def addTask[BR,RR](task: Task[Unit,Unit,BR,RR]): Task[Unit,Unit,BR,RR] = rootTask andThen task

  private final def execute(config: ScriptConfig): Unit = config.action match {
    case Action.Backup  => backup
    case Action.Restore => restore
  }
  
  private final def backup = rootTask performBackup
  
  private final def restore = rootTask performRestore

  def main(args: Array[String]): Unit = parser.parse(args, ScriptConfig()) foreach execute

  def backupFiles[PBR,PRR](files: List[String]) = BackupFiles[PBR,PRR](files)
}
