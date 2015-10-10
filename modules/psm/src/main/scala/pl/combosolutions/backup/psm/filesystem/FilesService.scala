package pl.combosolutions.backup.psm.filesystem

import java.nio.file.{ Files, Path }
import java.nio.file.StandardCopyOption._

import scala.util.Try

trait FilesService {

  def copy(from: Path, into: Path): Boolean

  def delete(file: Path): Boolean

  def move(from: Path, into: Path): Boolean
}

trait FilesServiceComponent {

  def filesService: FilesService
}

trait FilesServiceComponentImpl extends FilesServiceComponent {

  def filesService: FilesService = FilesServiceImpl

  trait FilesServiceImpl extends FilesService {

    private val withOptions = List(ATOMIC_MOVE, COPY_ATTRIBUTES, REPLACE_EXISTING)

    override def copy(from: Path, into: Path): Boolean = Try(Files copy (from, into, withOptions: _*)).isSuccess

    override def delete(file: Path): Boolean = Try(Files delete file).isSuccess

    override def move(from: Path, into: Path): Boolean = Try(Files move (from, into, withOptions: _*)).isSuccess
  }

  object FilesServiceImpl extends FilesServiceImpl
}

object FilesServiceComponentImpl extends FilesServiceComponentImpl
