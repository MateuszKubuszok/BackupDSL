package pl.combosolutions.backup.psm.filesystem

import org.specs2.mock.Mockito

trait TestFilesServiceComponent extends FilesServiceComponent with Mockito {

  val testFilesService = mock[FilesService]

  override lazy val filesService = testFilesService
}
