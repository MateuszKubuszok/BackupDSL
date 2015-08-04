package pl.combosolutions.backup.dsl.internals.filesystem

import java.io.File

import pl.combosolutions.backup.dsl.internals.filesystem.FSPath._

object FSPath {
  import java.io.File.separator

  val Current = "."
  val Up = ".."

  implicit def fileToFSPath(file: File): FSPath = fromAbsolute(file.getAbsolutePath)
  implicit def stringToFSPath(root: String): FSPath = fromAbsolute(root)

  private[internals] def fromAbsolute(root: String): FSPath = FSRoot(root)

  @scala.annotation.tailrec
  private[internals] def stringify(fSPath: FSPath, result: String): String = fSPath match {
    case FSRoot(root)              => s"$root$separator$result"
    case FSChild(parent, Current)  => stringify(parent, result)
    case FSChild(parent, Up)       => stringify(parent, s"$Up$separator$result") // TODO
    case FSChild(parent, basename) => stringify(parent, s"$basename$separator$result")
  }
}

sealed abstract trait FSPath {
  import FSPath.{Current, Up}

  lazy val asFile = new java.io.File(toString)

  def / (basename: String): FSPath = basename trim match {
    case Current => this
    case Up      => parent
    case _       => FSChild(this, basename)
  }
  def \ (basename: String): FSPath = / (basename)

  abstract val parent: FSPath
  abstract def toString: String
}

case class FSRoot(root: String) extends FSPath {
  override val parent   = FSChild(this, Up)
  override def toString = root
}

case class FSChild(parent: FSPath, basename: String) extends FSPath {
  override def toString = stringify(parent, basename)
}
