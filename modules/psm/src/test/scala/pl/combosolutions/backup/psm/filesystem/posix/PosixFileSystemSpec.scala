package pl.combosolutions.backup.psm.filesystem.posix

import java.io.File
import java.nio.file.Path

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.psm.commands.{ MoveCommand, DeleteCommand, CopyCommand, Command }
import pl.combosolutions.backup.psm.filesystem.FileType
import pl.combosolutions.backup.psm.filesystem.FileType.FileType
import pl.combosolutions.backup.psm.programs.posix.{ LinkFile, FileInfo }
import pl.combosolutions.backup.psm.systems.TestOperatingSystemComponent
import pl.combosolutions.backup.{ Async, Result }
import pl.combosolutions.backup.psm.elevation.{ TestElevationFacadeComponent, ObligatoryElevationMode }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.Program

import scala.reflect.ClassTag

class PosixFileSystemSpec extends Specification with Mockito {

  val component = new PosixFileSystemServiceComponent with TestElevationFacadeComponent with TestOperatingSystemComponent
  val service = component.fileSystemService
  val path = new File("test").getAbsoluteFile.toPath

  "PosixFileSystemService" should {

    "obtain file type" in new ProgramTestContext(classOf[FileInfo], classOf[FileType]) {
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

    "create symbolic link for file" in new ProgramTestContext(classOf[LinkFile], classOf[Boolean]) {
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

    "copy files from one place to another" in new CommandTestContext(classOf[CopyCommand], classOf[List[String]]) {
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

    "delete files" in new CommandTestContext(classOf[DeleteCommand], classOf[List[String]]) {
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

    "move files from one place to another" in new CommandTestContext(classOf[MoveCommand], classOf[List[String]]) {
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

  class CommandTestContext[CommandType <: Command[CommandType], ResultType](
      programClass: Class[CommandType],
      resultClass: Class[ResultType]) extends Scope {

    type InterpreterType = Result[CommandType]#Interpreter[ResultType]

    implicit val commandTag: ClassTag[CommandType] = ClassTag(programClass)
    implicit val resultTag: ClassTag[InterpreterType] = ClassTag(classOf[InterpreterType])

    val command = mock[Command[CommandType]]
    val elevationMode = mock[ObligatoryElevationMode]
    val cleaner = new Cleaner {}

    elevationMode[CommandType](any[CommandType], ===(cleaner)) returns command

    def makeDigestReturn(result: ResultType): Unit =
      command.digest[ResultType](any[InterpreterType]) returns Async.some(result)
  }

  class ProgramTestContext[ProgramType <: Program[ProgramType], ResultType](
      programClass: Class[ProgramType],
      resultClass: Class[ResultType]) extends Scope {

    type InterpreterType = Result[ProgramType]#Interpreter[ResultType]

    implicit val programTag: ClassTag[ProgramType] = ClassTag(programClass)
    implicit val resultTag: ClassTag[InterpreterType] = ClassTag(classOf[InterpreterType])

    val program = mock[Program[ProgramType]]
    val elevationMode = mock[ObligatoryElevationMode]
    val cleaner = new Cleaner {}

    elevationMode[ProgramType](any[ProgramType], ===(cleaner)) returns program

    def makeDigestReturn(result: ResultType): Unit =
      program.digest[ResultType](any[InterpreterType]) returns Async.some(result)
  }
}
