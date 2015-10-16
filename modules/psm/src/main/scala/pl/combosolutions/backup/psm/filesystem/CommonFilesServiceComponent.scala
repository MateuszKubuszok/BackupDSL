package pl.combosolutions.backup.psm.filesystem

import java.nio.file.{ CopyOption, Files, Path }

import pl.combosolutions.backup.Logging

import scala.util.{ Failure, Try }

trait CommonFilesServiceComponent extends FilesServiceComponent {

  def filesService: FilesService = FilesServiceImpl

  protected val available: Boolean

  protected val withCopyOptions: List[CopyOption]

  protected val withMoveOptions: List[CopyOption]

  trait FilesServiceImpl extends FilesService with Logging {

    override lazy val filesAvailable = available

    override def copy(from: Path, into: Path): Boolean =
      Try(Files copy (from, into, withCopyOptions: _*)) recover logError(s"Failed to copy '$from' -> '$into'") isSuccess

    override def delete(file: Path): Boolean =
      Try(Files delete file) recoverWith logError(s"Failed to delete '$file'") isSuccess

    override def move(from: Path, into: Path): Boolean =
      Try(Files move (from, into, withMoveOptions: _*)) recover logError(s"Failed to move '$from' -> '$into'") isSuccess

    private def logError[U](message: String): PartialFunction[Throwable, Try[U]] = {
      case ex: Throwable =>
        logger error (message, ex)
        Failure(ex)
    }
  }

  object FilesServiceImpl extends FilesServiceImpl
}
