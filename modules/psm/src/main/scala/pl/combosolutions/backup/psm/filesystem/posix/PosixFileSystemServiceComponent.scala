package pl.combosolutions.backup.psm.filesystem.posix

import java.nio.file.Path

import pl.combosolutions.backup._
import pl.combosolutions.backup.psm
import psm.elevation.{ ElevateIfNeeded, ElevationMode }
import ElevateIfNeeded._
import psm.filesystem.{ CommonFileSystemServiceComponent, FileSystemService, FileSystemServiceComponent }
import psm.filesystem.FileType.FileType
import psm.programs.posix.{ FileInfo, LinkFile, PosixPrograms }
import PosixPrograms._
import psm.systems.{ OperatingSystemComponent, OperatingSystemComponentImpl }

trait PosixFileSystemServiceComponent
    extends FileSystemServiceComponent
    with CommonFileSystemServiceComponent {
  self: FileSystemServiceComponent with OperatingSystemComponent =>

  override def fileSystemService: FileSystemService = PosixFileSystemService

  trait PosixFileSystemService extends FileSystemService with CommonFileSystemService {

    override lazy val fileSystemAvailable = operatingSystem.isPosix

    // format: OFF
    override def getFileType(forPath: Path)
                            (implicit withElevation: ElevationMode, cleaner: Cleaner): Async[FileType] =
      FileInfo(forPath.toAbsolutePath.toString).handleElevation.digest[FileType]

    override def isSupportingSymbolicLinks: Boolean = true

    override def linkFiles(files: List[(Path, Path)])
                          (implicit withElevation: ElevationMode, cleaner: Cleaner): Async[List[Path]] = {
      import ExecutionContexts.Command.context
      Async.completeSequence(paths2Strings(files) map { pair =>
        LinkFile(pair._1, pair._2).handleElevation.digest[Boolean].asAsync map { success =>
          if (success) string2Path(List(pair._1)) else List()
        }
      }).asAsync map (_.flatten)
    }
    // format: ON
  }

  object PosixFileSystemService extends PosixFileSystemService
}

object PosixFileSystemServiceComponent
  extends PosixFileSystemServiceComponent
  with OperatingSystemComponentImpl
