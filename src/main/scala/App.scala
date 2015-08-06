package example

import pl.combosolutions.backup.dsl.Script

object App extends Script("test script") {
  this addTask backupFiles("README.md")
}
