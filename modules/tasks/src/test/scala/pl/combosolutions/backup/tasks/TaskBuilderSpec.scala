package pl.combosolutions.backup.tasks

import java.nio.file.Path

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.Cleaner
import pl.combosolutions.backup.psm.ComponentRegistry
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, ObligatoryElevationMode }
import pl.combosolutions.backup.test.Tags.UnitTest

import scala.collection.mutable

class TaskBuilderSpec extends Specification with Mockito {

  "TaskBuilder" should {

    "replace build settings" in new TestContext {
      // given
      val newSettings: Settings = ImmutableSettings(
        cleaner                 = cleaner,
        withElevation           = elevation,
        withObligatoryElevation = obligatoryElevation,
        components              = components,
        backupDir               = backupDir
      )

      // when
      taskBuilder.updateSettings(newSettings)
      val result = taskBuilder.withSettings

      // then
      result.cleaner mustEqual cleaner
      result.withElevation mustEqual elevation
      result.withObligatoryElevation mustEqual obligatoryElevation
      result.components mustEqual components
      result.backupDir mustEqual backupDir
    } tag UnitTest

    "update build settings" in new TestContext {
      // given
      // when
      taskBuilder.updateSettings(
        cleaner                 = cleaner,
        withElevation           = elevation,
        withObligatoryElevation = obligatoryElevation,
        components              = components,
        backupDir               = backupDir
      )
      val result = taskBuilder.withSettings

      // then
      result.cleaner mustEqual cleaner
      result.withElevation mustEqual elevation
      result.withObligatoryElevation mustEqual obligatoryElevation
      result.components mustEqual components
      result.backupDir mustEqual backupDir
    } tag UnitTest
  }

  class TestContext extends Scope {

    val backup = mock[SubTask[String]]
    val backupProxy = new SubTaskProxy[String](DependencyType.Independent)
    val backupBuilder = mock[SubTaskBuilder[String, Unit, Unit]]
    val restore = mock[SubTask[String]]
    val restoreProxy = new SubTaskProxy[String](DependencyType.Independent)
    val restoreBuilder = mock[SubTaskBuilder[String, Unit, Unit]]
    val cleaner = mock[Cleaner]
    val elevation = mock[ElevationMode]
    val obligatoryElevation = mock[ObligatoryElevationMode]
    val components = mock[ComponentRegistry]
    val backupDir = mock[Path]

    backup.dependencyType returns DependencyType.Independent
    backup.getPropagation returns mutable.Set()
    backupProxy.setImplementation(backup)
    backupBuilder.injectableProxy returns backupProxy
    restore.dependencyType returns DependencyType.Independent
    restore.getPropagation returns mutable.Set()
    restoreProxy.setImplementation(restore)
    restoreBuilder.injectableProxy returns restoreProxy

    val taskBuilder = new TaskBuilder[String, Unit, Unit, String, Unit, Unit](backupBuilder, restoreBuilder)
  }
}
