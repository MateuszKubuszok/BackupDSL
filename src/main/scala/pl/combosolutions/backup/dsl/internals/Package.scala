package pl.combosolutions.backup.dsl.internals

sealed abstract trait Package
case class UnversionedPackage(name: String) extends  Package
case class VersionedPackage(name: String, version: String) extends Package
