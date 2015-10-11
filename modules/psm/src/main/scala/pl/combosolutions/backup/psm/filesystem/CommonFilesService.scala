package pl.combosolutions.backup.psm.filesystem

import java.nio.file.{ CopyOption, Files, Path }

import pl.combosolutions.backup.Logging

import scala.util.Try

trait CommonFilesServiceComponent extends FilesServiceComponent {

  def filesService: FilesService = FilesServiceImpl

  protected val available: Boolean

  protected val withCopyOptions: List[CopyOption]

  protected val withMoveOptions: List[CopyOption]

  trait FilesServiceImpl extends FilesService with Logging {

    override lazy val filesAvailable = available

    override def copy(from: Path, into: Path): Boolean = Try(Files copy (from, into, withCopyOptions: _*)) recover {
      case ex: Throwable =>
        logger error (s"Failed to copy '$from' '$into'", ex)
        ex
    } isSuccess

    override def delete(file: Path): Boolean = Try(Files delete file) recover {
      case ex: Throwable =>
        logger error (s"Failed to delete '$file'", ex)
        ex
    } isSuccess

    override def move(from: Path, into: Path): Boolean = Try(Files move (from, into, withMoveOptions: _*)) recover {
      case ex: Throwable =>
        logger error (s"Failed to move '$from' '$into'", ex)
        ex
    } isSuccess
  }

  object FilesServiceImpl extends FilesServiceImpl
}
