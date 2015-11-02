package pl.combosolutions.backup.tasks

import java.nio.file.Path

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.Cleaner
import pl.combosolutions.backup.psm.ComponentRegistry
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, ObligatoryElevationMode }
import pl.combosolutions.backup.tasks.DependencyType._
import pl.combosolutions.backup.test.Tags.UnitTest

import scala.collection.mutable

class TaskBuilderSpec extends Specification with Mockito {

  "TaskBuilder" should {

    "replace build settings" in new TestContext(Independent, Independent) {
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

    "update build settings" in new TestContext(Independent, Independent) {
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

    "build independent subtasks" in new TestContext(Independent, Independent) {
      // given
      val child = mock[TaskBuilder[Unit, String, Unit, Unit, String, Unit]]
      val childBackup = mock[SubTaskBuilder[Unit, String, Unit]]
      val childRestore = mock[SubTaskBuilder[Unit, String, Unit]]
      child.backupSubTaskBuilder returns childBackup
      child.restoreSubTaskBuilder returns childRestore
      childBackup.injectableProxy returns new SubTaskProxy(Independent)
      childRestore.injectableProxy returns new SubTaskProxy(Independent)
      taskBuilder.addChild(child)

      // when
      val result = taskBuilder.build

      // then
      there was one(backupBuilder).configurePropagation(Set(any[Propagator]))
      there was one(restoreBuilder).configurePropagation(Set(any[Propagator]))
    } tag UnitTest

    "build parent dependent subtasks" in new TestContext(ParentDependent, ParentDependent) {
      // given
      val child = mock[TaskBuilder[Unit, String, Unit, Unit, String, Unit]]
      val childBackup = mock[SubTaskBuilder[Unit, String, Unit]]
      val childRestore = mock[SubTaskBuilder[Unit, String, Unit]]
      child.backupSubTaskBuilder returns childBackup
      child.restoreSubTaskBuilder returns childRestore
      childBackup.injectableProxy returns new SubTaskProxy(Independent)
      childRestore.injectableProxy returns new SubTaskProxy(Independent)
      taskBuilder.addChild(child)
      val parent = mock[TaskBuilder[Unit, Unit, String, Unit, Unit, String]]
      val parentBackup = mock[SubTaskBuilder[Unit, Unit, String]]
      val parentRestore = mock[SubTaskBuilder[Unit, Unit, String]]
      parent.backupSubTaskBuilder returns parentBackup
      parent.restoreSubTaskBuilder returns parentRestore
      parentBackup.injectableProxy returns new SubTaskProxy(ParentDependent)
      parentRestore.injectableProxy returns new SubTaskProxy(ParentDependent)
      taskBuilder.setParent(parent)

      // when
      val result = taskBuilder.build

      // then
      there was one(backupBuilder).configureForParent(===(parentBackup))
      there was one(backupBuilder).configurePropagation(Set(any[Propagator]))
      there was one(restoreBuilder).configureForParent(===(parentRestore))
      there was one(restoreBuilder).configurePropagation(Set(any[Propagator]))
    } tag UnitTest

    "fail to build parent dependent subtasks without parent" in new TestContext(ParentDependent, ParentDependent) {
      // given
      // when
      taskBuilder.build must throwA[IllegalStateException]
    } tag UnitTest

    "build child dependent subtasks" in new TestContext(ChildDependent, ChildDependent) {
      // given
      val child = mock[TaskBuilder[Unit, String, Unit, Unit, String, Unit]]
      val childBackup = mock[SubTaskBuilder[Unit, String, Unit]]
      val childRestore = mock[SubTaskBuilder[Unit, String, Unit]]
      child.backupSubTaskBuilder returns childBackup
      child.restoreSubTaskBuilder returns childRestore
      childBackup.injectableProxy returns new SubTaskProxy(ChildDependent)
      childRestore.injectableProxy returns new SubTaskProxy(ChildDependent)
      taskBuilder.addChild(child)

      // when
      val result = taskBuilder.build

      // then
      there was one(backupBuilder).configureForChildren(===(List(childBackup)))
      there was one(restoreBuilder).configureForChildren(===(List(childRestore)))
    } tag UnitTest
  }

  class TestContext(backupType: DependencyType, restoreType: DependencyType) extends Scope {

    val backup = mock[SubTask[String]]
    val backupProxy = new SubTaskProxy[String](backupType)
    val backupBuilder = mock[SubTaskBuilder[String, Unit, Unit]]
    val restore = mock[SubTask[String]]
    val restoreProxy = new SubTaskProxy[String](restoreType)
    val restoreBuilder = mock[SubTaskBuilder[String, Unit, Unit]]
    val cleaner = mock[Cleaner]
    val elevation = mock[ElevationMode]
    val obligatoryElevation = mock[ObligatoryElevationMode]
    val components = mock[ComponentRegistry]
    val backupDir = mock[Path]

    backup.dependencyType returns backupType
    backup.getPropagation returns mutable.Set()
    backupProxy.setImplementation(backup)
    backupBuilder.injectableProxy returns backupProxy
    restore.dependencyType returns restoreType
    restore.getPropagation returns mutable.Set()
    restoreProxy.setImplementation(restore)
    restoreBuilder.injectableProxy returns restoreProxy

    val taskBuilder = new TaskBuilder[String, Unit, Unit, String, Unit, Unit](backupBuilder, restoreBuilder)
  }
}
