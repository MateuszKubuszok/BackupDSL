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

    "be digested to Boolean with built-in Interpreter" in new TestCopyContext {
      // given
      import CommonCommands.CopyCommand2Boolean
      val expected = true

      // when
      val result = command.digest[Boolean]

      // then
      result must beSome(expected).await
    } tag UnitTest

    "be digested to List[String] with built-in Interpreter" in new TestCopyContext {
      // given
      import CommonCommands.CopyCommand2List
      val expected1 = List(fromFileName)
      val expected2 = List[String]()

      // when
      val result1 = command.digest[List[String]]
      val result2 = command2.digest[List[String]]

      // then
      result1 must beSome(expected1).await
      result2 must beSome(expected2).await
    } tag UnitTest

    "be digested to (List[String],List[String]) with built-in Interpreter" in new TestCopyContext {
      // given
      import CommonCommands.CopyCommand2Tuple
      val expected1 = (List(fromFileName), List[String]())
      val expected2 = (List[String](), List(fromFileName))

      // when
      val result1 = command.digest[(List[String], List[String])]
      val result2 = command2.digest[(List[String], List[String])]

      // then
      result1 must beSome(expected1).await
      result2 must beSome(expected2).await
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

    "be digested to Boolean with built-in Interpreter" in new TestDeleteContext {
      // given
      import CommonCommands.DeleteCommand2Boolean
      val expected = true

      // when
      val result = command.digest[Boolean]

      // then
      result must beSome(expected).await
    } tag UnitTest

    "be digested to List[String] with built-in Interpreter" in new TestDeleteContext {
      // given
      import CommonCommands.DeleteCommand2List
      val expected1 = List(fileName)
      val expected2 = List[String]()

      // when
      val result1 = command.digest[List[String]]
      val result2 = command2.digest[List[String]]

      // then
      result1 must beSome(expected1).await
      result2 must beSome(expected2).await
    } tag UnitTest

    "be digested to (List[String],List[String]) with built-in Interpreter" in new TestDeleteContext {
      // given
      import CommonCommands.DeleteCommand2Tuple
      val expected1 = (List(fileName), List[String]())
      val expected2 = (List[String](), List(fileName))

      // when
      val result1 = command.digest[(List[String], List[String])]
      val result2 = command2.digest[(List[String], List[String])]

      // then
      result1 must beSome(expected1).await
      result2 must beSome(expected2).await
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

    "be digested to Boolean with built-in Interpreter" in new TestMoveContext {
      // given
      import CommonCommands.MoveCommand2Boolean
      val expected = true

      // when
      val result1 = command.digest[Boolean]

      // then
      result1 must beSome(expected).await
    } tag UnitTest

    "be digested to List[String] with built-in Interpreter" in new TestMoveContext {
      // given
      import CommonCommands.MoveCommand2List
      val expected1 = List(fromFileName)
      val expected2 = List[String]()

      // when
      val result1 = command.digest[List[String]]
      val result2 = command2.digest[List[String]]

      // then
      result1 must beSome(expected1).await
      result2 must beSome(expected2).await
    } tag UnitTest

    "be digested to (List[String],List[String]) with built-in Interpreter" in new TestMoveContext {
      // given
      import CommonCommands.MoveCommand2Tuple
      val expected1 = (List(fromFileName), List[String]())
      val expected2 = (List[String](), List(fromFileName))

      // when
      val result1 = command.digest[(List[String], List[String])]
      val result2 = command2.digest[(List[String], List[String])]

      // then
      result1 must beSome(expected1).await
      result2 must beSome(expected2).await
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
    val command2 = new CopyCommand(input) with TestFilesServiceComponent
    command2.testFilesService.copy(any[Path], any[Path]) returns false
  }

  trait TestDeleteContext extends TestContext {

    val input = List(fileName)
    val command = new DeleteCommand(input) with TestFilesServiceComponent
    command.testFilesService.delete(any[Path]) returns true
    val command2 = new DeleteCommand(input) with TestFilesServiceComponent
    command2.testFilesService.delete(any[Path]) returns false
  }

  trait TestMoveContext extends TestContext {

    val input = List((fromFileName, intoFileName))
    val command = new MoveCommand(input) with TestFilesServiceComponent
    command.testFilesService.move(any[Path], any[Path]) returns true
    val command2 = new MoveCommand(input) with TestFilesServiceComponent
    command2.testFilesService.move(any[Path], any[Path]) returns false
  }
}
