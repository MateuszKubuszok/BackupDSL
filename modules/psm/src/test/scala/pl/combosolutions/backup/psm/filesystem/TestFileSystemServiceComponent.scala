package pl.combosolutions.backup.psm.filesystem

import org.specs2.mock.Mockito

trait TestFileSystemServiceComponent extends FileSystemServiceComponent with Mockito {

  val testFileSystemService = mock[FileSystemService]

  override def fileSystemService = testFileSystemService
}

