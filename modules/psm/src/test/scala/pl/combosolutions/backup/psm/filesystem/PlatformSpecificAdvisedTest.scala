package pl.combosolutions.backup.psm.filesystem

import java.io.File
import java.io.File.separator

import org.specs2.mutable.BeforeAfter
import pl.combosolutions.backup.psm.PlatformSpecificSpecification
import pl.combosolutions.backup.psm.elevation.{ NotElevated, ElevationTestHelper }
import pl.combosolutions.backup.test.Tags.{ DisabledTest, PlatformTest }

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf

class PlatformSpecificAdvisedTest
    extends PlatformSpecificSpecification
    with ElevationTestHelper {

  "Current platform's file system" should {

    "get directory/file type" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner
      val directory = makeDir(testDirectoryPath)
      val file = makeFile(testFilePath)

      // when
      val directoryResult = fileSystemService getFileType directory.toPath
      val fileResult = fileSystemService getFileType file.toPath

      // then
      directoryResult must beSome(FileType.Directory).await
      fileResult must beSome(FileType.File).await
    } tag PlatformTest

    "create symlink and get symlink type" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner
      val fromFile = makeFile(testFilePath)
      val toFile = tmpFile(testLinkPath)

      // when
      val linkingResult = fileSystemService linkFiles List((fromFile.toPath, toFile.toPath))
      Await result (linkingResult, Inf)
      val typeResult = fileSystemService getFileType toFile.toPath

      // then
    } tag (if (fileSystemService.isSupportingSymbolicLinks) PlatformTest else DisabledTest)

    "copy files from one location to another" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner
      val fromDirectory = makeFile(testDirectoryPath)
      val toDirectory = tmpFile(testNewDirectoryLocation)
      val fromFile = makeFile(testFilePath)
      val toFile = tmpFile(testNewFileLocation)

      // when
      val result = fileSystemService copyFiles List(
        (fromDirectory.toPath, toDirectory.toPath),
        (fromFile.toPath, toFile.toPath)
      )

      // then
      result must beSome(List(fromDirectory.toPath, fromFile.toPath)).await
    } tag PlatformTest

    "delete files from location" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner
      val directory = makeDir(testDirectoryPath)
      val file = makeFile(testFilePath)

      // when
      val result = fileSystemService deleteFiles List(directory.toPath, file.toPath)

      // then
      result must beSome(List(directory.toPath, file.toPath)).await
    } tag PlatformTest

    "move files from one location to another" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner
      val fromDirectory = makeFile(testDirectoryPath)
      val toDirectory = tmpFile(testNewDirectoryLocation)
      val fromFile = makeFile(testFilePath)
      val toFile = tmpFile(testNewFileLocation)

      // when
      val result = fileSystemService moveFiles List(
        (fromDirectory.toPath, toDirectory.toPath),
        (fromFile.toPath, toFile.toPath)
      )

      // then
      result must beSome(List(fromDirectory.toPath, fromFile.toPath)).await
    } tag PlatformTest
  }

  trait TestContext extends BeforeAfter {

    val withElevation = NotElevated
    val cleaner = ElevationTestCleaner

    val testDirectoryPath = "test-directory"
    val testFilePath = "test-file"
    val testLinkPath = "test-link"
    val testNewDirectoryLocation = "test-new-directory-location"
    val testNewFileLocation = "test-new-file-location"

    def tmpFile(fileName: String): File = new File(s"${System getProperty "java.io.tmpdir"}$separator$fileName")

    def makeDir(fileName: String): File = {
      val file = tmpFile(fileName)
      file.mkdirs
      file
    }

    def makeFile(fileName: String): File = {
      val file = tmpFile(fileName)
      file.createNewFile
      file
    }

    override def before: Any = fileCleanUp

    override def after: Any = {
      fileCleanUp
      ElevationTestCleaner.cleanup
    }

    private val fileNamesToClean =
      List(testDirectoryPath, testFilePath, testLinkPath, testNewDirectoryLocation, testNewFileLocation)

    private def fileCleanUp: Unit =
      fileNamesToClean map tmpFile foreach { file => if (file.exists) file.delete }
  }
}
