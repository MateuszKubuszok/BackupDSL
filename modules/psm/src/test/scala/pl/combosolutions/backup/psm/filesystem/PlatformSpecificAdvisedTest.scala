package pl.combosolutions.backup.psm.filesystem

import java.nio.file.{ Files, Path, Paths }

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
      val directoryResult = fileSystemService getFileType directory
      val fileResult = fileSystemService getFileType file

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
      val linkingResult = fileSystemService linkFiles List((fromFile, toFile))
      Await result (linkingResult, Inf)
      val typeResult = fileSystemService getFileType toFile

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
        (fromDirectory, toDirectory),
        (fromFile, toFile)
      )

      // then
      result must beSome(List(fromDirectory, fromFile)).await
    } tag PlatformTest

    "delete files from location" in new TestContext {
      // given
      implicit val e = withElevation
      implicit val c = cleaner
      val directory = makeDir(testDirectoryPath)
      val file = makeFile(testFilePath)

      // when
      val result = fileSystemService deleteFiles List(directory, file)

      // then
      result must beSome(List(directory, file)).await
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
        (fromDirectory, toDirectory),
        (fromFile, toFile)
      )

      // then
      result must beSome(List(fromDirectory, fromFile)).await
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

    def tmpFile(fileName: String): Path = Paths.get(System getProperty "java.io.tmpdir", fileName)

    def makeDir(fileName: String): Path = Files createDirectories tmpFile(fileName)

    def makeFile(fileName: String): Path = Files createFile tmpFile(fileName)

    override def before: Any = fileCleanUp

    override def after: Any = {
      fileCleanUp
      ElevationTestCleaner.cleanup
    }

    private val fileNamesToClean =
      List(testDirectoryPath, testFilePath, testLinkPath, testNewDirectoryLocation, testNewFileLocation)

    private def fileCleanUp(): Unit =
      fileNamesToClean map tmpFile foreach { file => if (Files exists file) Files delete file }
  }
}
