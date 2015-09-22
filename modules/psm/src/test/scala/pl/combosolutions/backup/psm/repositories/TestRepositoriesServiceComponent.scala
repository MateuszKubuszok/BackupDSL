package pl.combosolutions.backup.psm.repositories

import org.specs2.mock.Mockito

trait TestRepositoriesServiceComponent extends RepositoriesServiceComponent with Mockito {

  val testRepositoriesService = mock[RepositoriesService]

  override def repositoriesService = testRepositoriesService
}
