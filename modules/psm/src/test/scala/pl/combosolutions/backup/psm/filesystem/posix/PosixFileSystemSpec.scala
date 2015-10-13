package pl.combosolutions.backup.psm.filesystem.posix

import java.io.File

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.commands._
import pl.combosolutions.backup.psm.filesystem.FileType
import pl.combosolutions.backup.psm.filesystem.FileType.FileType
import pl.combosolutions.backup.psm.programs.posix.{ LinkFile, FileInfo }
import pl.combosolutions.backup.psm.systems.TestOperatingSystemComponent
import pl.combosolutions.backup.psm.elevation.TestElevationFacadeComponent
import pl.combosolutions.backup.psm.programs.ProgramContextHelper

class PosixFileSystemSpec
    extends Specification
    with Mockito
    with CommandContextHelper
    with ProgramContextHelper {

  val component = new PosixFileSystemServiceComponent with TestElevationFacadeComponent with TestOperatingSystemComponent
  val service = component.fileSystemService
  val path = new File("test").getAbsoluteFile.toPath

  "PosixFileSystemService" should {

    "obtain file type" in new ProgramContext(classOf[FileInfo], classOf[FileType]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val expected = FileType.File
      makeDigestReturn(expected)

      // when
      val result = service getFileType path

      // then
      result must beSome(expected).await
    }

    "create symbolic link for file" in new ProgramContext(classOf[LinkFile], classOf[Boolean]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val expected = List(path)
      makeDigestReturn(true)

      // when
      val result = service linkFiles List((path, path))

      // then
      result must beSome(expected).await
    }

    "copy files from one place to another" in new CommandContext(classOf[CopyCommand], classOf[List[String]]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val expected = List(path)
      makeDigestReturn(List(path.toString))

      // when
      val result = service copyFiles List((path, path))

      // then
      result must beSome(expected).await
    }

    "delete files" in new CommandContext(classOf[DeleteCommand], classOf[List[String]]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val expected = List(path)
      makeDigestReturn(List(path.toString))

      // when
      val result = service deleteFiles List(path)

      // then
      result must beSome(expected).await
    }

    "move files from one place to another" in new CommandContext(classOf[MoveCommand], classOf[List[String]]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val expected = List(path)
      makeDigestReturn(List(path.toString))

      // when
      val result = service moveFiles List((path, path))

      // then
      result must beSome(expected).await
    }
  }
}
