package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.dsl.ActionRead._
import pl.combosolutions.backup.tasks.Action._

trait ArgumentParser {

  val name: String

  protected val parser = new scopt.OptionParser[ScriptConfig](name) {
    head("backup/restore script made with BackupDSL")

    (arg[Action]("action")
      action { (action, conf) => conf.copy(action = action) }
      text "whether to perform backup or restore"
      required ())

    (help("help")
      action { (action, conf) => conf.copy(showHelp = true) }
      text "displays help")
  }
}
