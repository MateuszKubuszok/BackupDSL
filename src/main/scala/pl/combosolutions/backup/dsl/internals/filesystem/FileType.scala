package pl.combosolutions.backup.dsl.internals.filesystem

object FileType extends Enumeration {
  type FileType = Value
  val File, Directory, SymbolicLink = Value
}

