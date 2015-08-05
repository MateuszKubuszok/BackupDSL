package pl.combosolutions.backup.dsl.internals.repositories

sealed abstract class Package(val name: String) { override def toString = name }
case class UnversionedPackage(override val name: String) extends Package(name)
case class VersionedPackage(override val name: String, version: String) extends Package(name)
