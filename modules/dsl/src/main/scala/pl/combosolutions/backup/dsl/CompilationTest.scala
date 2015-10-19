package pl.combosolutions.backup.dsl

// $COVERAGE-OFF$Throw away code used for fast tests before proper ones are written
object CompilationTest extends Script("Test script") {

  configuration forThis { config =>

    config.selectFiles forThis { selection =>

      selection.files += "README.md"
      selection.files += "ROADMAP.md"

      selection.backupFiles
    }
  }
}
// $COVERAGE-ON$
