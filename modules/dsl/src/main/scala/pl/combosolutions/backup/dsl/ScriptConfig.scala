package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.tasks.Action._

case class ScriptConfig(
  showHelp: Boolean = false,
  showTime: Boolean = true,
  action:   Action  = No
)
