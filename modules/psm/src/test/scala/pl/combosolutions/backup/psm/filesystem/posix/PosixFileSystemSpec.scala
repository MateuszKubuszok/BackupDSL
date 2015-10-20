package pl.combosolutions.backup.psm.filesystem.posix

import java.io.File
import java.nio.file.Path

import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.commands._
import pl.combosolutions.backup.psm.filesystem.FileType
import pl.combosolutions.backup.psm.filesystem.FileType.FileType
import pl.combosolutions.backup.psm.programs.posix.{ LinkFile, FileInfo }
import pl.combosolutions.backup.psm.systems._
import pl.combosolutions.backup.psm.elevation.TestElevationFacadeComponent
import pl.combosolutions.backup.psm.programs.ProgramContextHelper
import pl.combosolutions.backup.test.Tags.UnitTest

class PosixFileSystemSpec
    extends Specification
    with Mockito
    with CommandContextHelper
    with ProgramContextHelper {

  val component = new PosixFileSystemServiceComponent with TestElevationFacadeComponent with TestOperatingSystemComponent
  val service = component.fileSystemService
  val path = new File("test").getAbsoluteFile.toPath

  "PosixFileSystemService" should {

    "correctly calculate availability" in new PosixFileSystemServiceComponentResolutionTestContext {
      // given
      // when
      val availabilityForPosix = serviceForPosix.fileSystemService.fileSystemAvailable
      val availabilityForWindows = serviceForWindows.fileSystemService.fileSystemAvailable

      // then
      availabilityForPosix mustEqual true
      availabilityForWindows mustEqual false
    } tag UnitTest

    "correctly calculate priority" in new PosixFileSystemServiceComponentResolutionTestContext {
      // given
      // when
      val availabilityForPosix = serviceForPosix.fileSystemService.fileSystemPriority
      val availabilityForWindows = serviceForWindows.fileSystemService.fileSystemPriority

      // then
      availabilityForPosix mustEqual OnlyAllowed
      availabilityForWindows mustEqual NotAllowed
    } tag UnitTest

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
    } tag UnitTest

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
    } tag UnitTest

    "fail to create symbolic link for file" in new ProgramContext(classOf[LinkFile], classOf[Boolean]) {
      // given
      implicit val e = elevationMode
      implicit val c = cleaner
      val expected = List[Path]()
      makeDigestReturn(false)

      // when
      val result = service linkFiles List((path, path))

      // then
      result must beSome(expected).await
    } tag UnitTest

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
    } tag UnitTest

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
    } tag UnitTest

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
    } tag UnitTest
  }

  trait PosixFileSystemServiceComponentResolutionTestContext extends Scope {

    val serviceForPosix = new TestPosixFileSystemServiceComponent(DebianSystem)
    val serviceForWindows = new TestPosixFileSystemServiceComponent(Windows7System)
  }

  class TestPosixFileSystemServiceComponent(override val operatingSystem: OperatingSystem)
    extends PosixFileSystemServiceComponent
    with OperatingSystemComponent
}
