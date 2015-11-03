# Backup DSL

[![Build status](https://api.shippable.com/projects/561d0f141895ca44741d627e/badge/master)](https://app.shippable.com/projects/561d0f141895ca44741d627e)

Project of a DSL for performing backup and restore of some specific parts of
the system.

## Example script

Simple backup script looks like this: 

    package example

    import pl.combosolutions.backup.dsl.Script
    
    object ExampleScript extends Script("Test script") {
    
      configuration forThis { config =>
    
        config.selectFiles forThis { selection =>
    
          selection.files += "README.md"
          selection.files += "ROADMAP.md"
    
          selection.backupFiles
        }
      }
    }

One can run it with `sbt "runMain example.ExampleScript backup"` and
`sbt "runMain example.ExampleScript restore"`. Script will try to copy
`README.md` and `ROADMAP.md` files into default backup directory.

Using [SBT script runner](http://www.scala-sbt.org/0.13/docs/Scripts.html) you
might try to publish artifacts (`sbt publishLocal`) and then create backup
configuration like this one:

    #!/usr/bin/env scalas
    
    /***
    scalaVersion := "2.11.7"
    
    libraryDependencies += "pl.combosolutions" %% "backup-dsl" % "0.2.0-SNAPSHOT"
    */
    
    import pl.combosolutions.backup.dsl.Script
    
    object ExampleScript extends Script("Test script") {
    
      configuration forThis { config =>
    
        config.selectFiles forThis { selection =>
    
          selection.files += "README.md"
          selection.files += "ROADMAP.md"
    
          selection.backupFiles
        }
      }
    }

## Development

Build process requires [SBT](www.scala-sbt.org) or
[Activator](https://www.typesafe.com/activator/download) installed. Code is
automatically formatted on compile task by scalariform plugin.

Components that have no clear idea behind them yet (prototypes) can go untested
until good API is figured out. Then they should be completely unit tested with
emphasis on corner cases.

Platform Specific Model module development requires some specific guidelines
- read more [here](PSM-DEVELOPMENT.md).

## Roadmap

See roadmap [here](ROADMAP.md)

## License

MIT license, see [License](LICENSE).
