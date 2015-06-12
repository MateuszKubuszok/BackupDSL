package pl.combosolutions.backup.dsl.internals

sealed abstract trait Repository
case class AptRepository(isSrc: Boolean, url: String, branches: List[String]) extends Repository
