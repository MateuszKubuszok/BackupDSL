package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.{ Cleaner, Logging }
import pl.combosolutions.backup.tasks.Action._
import pl.combosolutions.backup.tasks.ImmutableSettings

import scala.util.{ Failure, Success, Try }

abstract class Script(name: String) extends Cleaner with Logging {

  private val parser = new scopt.OptionParser[ScriptConfig](name) {
    head("backup/restore script made with BackupDSL")

    (arg[Action]("action")
      action { (action, conf) => conf.copy(action = action) }
      text "whether to perform backup or restore"
      required ())

    (help("help")
      action { (action, conf) => conf.copy(showHelp = true) }
      text "displays help")

    // pass variables into the Script: var=val
  }

  private val initialSettings = ImmutableSettings(cleaner = this)

  val configuration = new Root(initialSettings)

  private final def execute(config: ScriptConfig): Unit = config.action match {
    case Backup =>
      logger info s"Running BACKUP: $name"
      logger trace s"with configuration $config"
      configuration.buildTasks.backup
    case Restore =>
      logger info s"Running RESTORE: $name"
      logger trace s"with configuration $config"
      configuration.buildTasks.restore
    case _ =>
      if (config.showHelp) parser.showTryHelp
      else parser.reportError("backup/restore is required option!! Try --help for more information")
  }

  def main(args: Array[String]): Unit = {
    Try (parser parse (args, ScriptConfig()) foreach execute) match {
      case Failure(ex) => logger error ("error during execution", ex)
      case _           =>
    }
    clean
  }
}
