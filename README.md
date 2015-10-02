# Backup DSL

Project of a DSL for performing backup and restore of some specific parts of
the system.

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
