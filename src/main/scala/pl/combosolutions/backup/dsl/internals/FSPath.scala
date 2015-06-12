package pl.combosolutions.backup.dsl.internals

object FSPath {
  import java.io.File.separator

  val Current = "."
  val Up = ".."

  def fromAbsolute(root: String): FSPath = FSRoot(root)

  @scala.annotation.tailrec
  def stringify(fSPath: FSPath, result: String): String = fSPath match {
    case FSRoot(root) => s"$root$separator$result"
    case FSChild(parent, basename) => stringify(parent, s"$basename$separator$result")
  }
}

sealed abstract trait FSPath {
  import FSPath.{Current, Up}

  lazy val asFile = java.io.File(toString)

  def / (basename: String): FSPath = basename match {
    case Current => self
    case Up => parent
    case _ => FSChild(self, basename)
  }

  abstract def parent: FSPath
  abstract def toString: String
}
case class FSRoot(root: String) extends FSPath {
  import FSPath.Up
  override def parent = FSChild(self, Up)
  override def toString = root
}
case class FSChild(parent: FSPath, basename: String) extends FSPath {
  import FSPath.stringify
  override def parent = self.parent
  override def toString = stringify(parent, basename)
}