package pl.combosolutions.backup.psm.repositories

sealed abstract class Package(val name: String) { override def toString: String = name }
case class NonVersionedPackage(override val name: String) extends Package(name)
case class VersionedPackage(override val name: String, version: String) extends Package(name)
