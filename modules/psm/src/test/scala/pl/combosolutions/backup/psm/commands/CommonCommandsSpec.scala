package pl.combosolutions.backup.psm.commands

import java.io.File
import java.nio.file.Path

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.Result
import pl.combosolutions.backup.psm.filesystem.TestFilesServiceComponent
import pl.combosolutions.backup.test.Tags.UnitTest

class CommonCommandsSpec extends Specification with Mockito {

  "CopyCommand" should {

    "copy files from one location to another" in new TestCopyContext {
      // given
      // when
      val result = command.run

      // then
      result must beSome(Result[CopyCommand](0, List(fromFileName), List())).await
      there was one(command.testFilesService).copy(===(fromPath), ===(intoPath))
    } tag UnitTest
  }

  "DeleteCommand" should {

    "delete files" in new TestDeleteContext {
      // given
      // when
      val result = command.run

      // then
      result must beSome(Result[DeleteCommand](0, List(fileName), List())).await
      there was one(command.testFilesService).delete(===(filePath))
    } tag UnitTest
  }

  "MoveCommand" should {

    "move files from one location to another" in new TestMoveContext {
      // given
      // when
      val result = command.run

      // then
      result must beSome(Result[MoveCommand](0, List(fromFileName), List())).await
      there was one(command.testFilesService).move(===(fromPath), ===(intoPath))
    } tag UnitTest
  }

  trait TestContext extends Scope {

    val fileName = "file"
    val fromFileName = "from-file"
    val intoFileName = "into-file"
    val filePath = new File(fileName).getAbsoluteFile.toPath
    val fromPath = new File(fromFileName).getAbsoluteFile.toPath
    val intoPath = new File(intoFileName).getAbsoluteFile.toPath
  }

  trait TestCopyContext extends TestContext {

    val input = List((fromFileName, intoFileName))
    val command = new CopyCommand(input) with TestFilesServiceComponent
    command.testFilesService.copy(any[Path], any[Path]) returns true
  }

  trait TestDeleteContext extends TestContext {

    val input = List(fileName)
    val command = new DeleteCommand(input) with TestFilesServiceComponent
    command.testFilesService.delete(any[Path]) returns true
  }

  trait TestMoveContext extends TestContext {

    val input = List((fromFileName, intoFileName))
    val command = new MoveCommand(input) with TestFilesServiceComponent
    command.testFilesService.move(any[Path], any[Path]) returns true
  }
}
