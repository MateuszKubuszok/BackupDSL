package pl.combosolutions.backup.psm.filesystem

import java.nio.file.Path

import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.ImplementationResolver
import pl.combosolutions.backup.psm.PsmExceptionMessages._
import pl.combosolutions.backup.psm.filesystem.posix.PosixFilesServiceComponent

import FilesServiceComponentImpl.resolve

trait FilesService {

  val filesAvailable: Boolean

  def copy(from: Path, into: Path): Boolean

  def delete(file: Path): Boolean

  def move(from: Path, into: Path): Boolean
}

trait FilesServiceComponent {

  def filesService: FilesService
}

object FilesServiceComponentImpl extends ImplementationResolver[FilesService] {

  override lazy val implementations = Seq(
    // POSIX file system
    PosixFilesServiceComponent.filesService
  )

  override lazy val notFoundMessage = NoFileSystemAvailable

  override def byFilter(service: FilesService): Boolean = service.filesAvailable

  // TODO: improve
  override def byPriority(service: FilesService): ImplementationPriority =
    if (service.filesAvailable) Allowed
    else NotAllowed
}

trait FilesServiceComponentImpl extends FilesServiceComponent {

  override lazy val filesService = resolve
}
