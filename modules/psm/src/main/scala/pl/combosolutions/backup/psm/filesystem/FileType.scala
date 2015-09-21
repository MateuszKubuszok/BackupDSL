package pl.combosolutions.backup.psm.filesystem

object FileType extends Enumeration {
  type FileType = Value
  val File, Directory, SymbolicLink = Value
}

