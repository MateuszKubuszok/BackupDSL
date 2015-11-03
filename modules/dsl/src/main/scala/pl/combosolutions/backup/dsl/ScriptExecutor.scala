package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.{ Cleaner, Logging }
import pl.combosolutions.backup.tasks.Action._
import pl.combosolutions.backup.tasks.ImmutableSettings

trait ScriptExecutor {
  self: Cleaner with Logging =>

  val name: String

  def printHelp(): Unit

  private val initialSettings = ImmutableSettings(cleaner = this)

  final val configuration = new Root(initialSettings)

  protected final def execute(config: ScriptConfig): Unit = config.action match {
    case Backup =>
      logger info s"Running BACKUP: $name"
      logger trace s"with configuration $config"
      configuration.buildTasks.backup
    case Restore =>
      logger info s"Running RESTORE: $name"
      logger trace s"with configuration $config"
      configuration.buildTasks.restore
    case _ =>
      if (config.showHelp) printHelp
      else logger error "backup/restore is required option!! Try --help for more information"
  }
}
