package pl.combosolutions.backup.dsl.internals.repositories

sealed abstract trait Repository

case class AptRepository(isSrc: Boolean, url: String, branches: List[String]) extends Repository {
  override def toString = s"deb${if (isSrc) "-src" else ""} $url ${branches reduce (_ + " "+ _)}"
}
