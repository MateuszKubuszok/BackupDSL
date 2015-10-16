package pl.combosolutions.backup.dsl

object CompilationTest extends Script("Test script") {

  configuration forThis { config =>

    config.selectFiles forThis { selection =>

      selection.files += "README.md"
      selection.files += "ROADMAP.md"

      selection.backupFiles
    }
  }
}
