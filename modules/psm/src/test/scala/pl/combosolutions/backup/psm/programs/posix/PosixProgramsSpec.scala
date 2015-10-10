package pl.combosolutions.backup.psm.programs.posix

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.AsyncResult
import pl.combosolutions.backup.psm.filesystem.FileType.{ Directory, File, FileType, SymbolicLink }
import pl.combosolutions.backup.psm.programs.{ Result, TestProgramHelper }
import pl.combosolutions.backup.test.Tags.UnitTest

class PosixProgramsSpec extends Specification with Mockito {

  "CatFile" should {

    "create cat command" in {
      // given
      val file = "test-file"

      // when
      val program = CatFile(file)

      // then
      program.name mustEqual "cat"
      program.arguments mustEqual List(file)
    } tag UnitTest

    "be digested to List[String] with built-in Interpreter" in {
      // given
      import PosixPrograms.CatFile2Content
      val expected = List("test1", "test2")
      val program = new CatFile("test") with TestProgramHelper[CatFile]
      program.result = AsyncResult some (Result[CatFile](0, expected, List()))

      // when
      val result = program.digest[List[String]]

      // then
      result must beSome(expected).await
    } tag UnitTest
  }

  "FileInfo" should {

    "create file command" in {
      // given
      val file = "test-file"

      // when
      val program = FileInfo(file)

      // then
      program.name mustEqual "file"
      program.arguments mustEqual List(file)
    } tag UnitTest

    "be digested to FileType with built-in Interpreter" in {
      // given
      import PosixPrograms.FileInfo2FileType
      val expected1 = Directory
      val program1 = new FileInfo("test-file") with TestProgramHelper[FileInfo]
      program1.result = AsyncResult some (Result[FileInfo](0, List("test-file: directory"), List()))
      val expected2 = SymbolicLink
      val program2 = new FileInfo("test-file") with TestProgramHelper[FileInfo]
      program2.result = AsyncResult some (Result[FileInfo](0, List("test-file: symbolic link to sym-link"), List()))
      val expected3 = File
      val program3 = new FileInfo("test-file") with TestProgramHelper[FileInfo]
      program3.result = AsyncResult some (Result[FileInfo](0, List("test-file: sth else"), List()))

      // when
      val result1 = program1.digest[FileType]
      val result2 = program2.digest[FileType]
      val result3 = program3.digest[FileType]

      // then
      result1 must beSome(expected1).await
      result2 must beSome(expected2).await
      result3 must beSome(expected3).await
    } tag UnitTest
  }

  "GrepFile" should {

    "create grep command" in {
      // given
      val pattern = "test-pattern"
      val file = "test-file"

      // when
      val program = GrepFiles(pattern, List(file))

      // then
      program.name mustEqual "grep"
      program.arguments mustEqual List("-h", pattern, file)
    } tag UnitTest

    "be digested to List[String] with built-in Interpreter" in {
      // given
      import PosixPrograms.GrepFiles2ListString
      val expected = List("test1", "test2")
      val program = new GrepFiles("test-pattern", List("test-file")) with TestProgramHelper[GrepFiles]
      program.result = AsyncResult some (Result[GrepFiles](0, expected, List()))

      // when
      val result = program.digest[List[String]]

      // then
      result must beSome(expected).await
    } tag UnitTest
  }

  "WhichProgram" should {

    "create which command" in {
      // given
      val executable = "program"

      // when
      val program = WhichProgram(executable)

      // then
      program.name mustEqual "which"
      program.arguments mustEqual List(executable)
    } tag UnitTest

    "be digested to Boolean with built-in Interpreter" in {
      // given
      import PosixPrograms.WhichProgram2Boolean
      val expected = true
      val program = new WhichProgram("test") with TestProgramHelper[WhichProgram]

      // when
      val result = program.digest[Boolean]

      // then
      result must beSome(expected).await
    } tag UnitTest
  }
}
