package pl.combosolutions.backup.tasks

import java.nio.file.{ CopyOption, Path }

import pl.combosolutions.backup.psm.ComponentRegistry
import pl.combosolutions.backup.psm.elevation.{ ObligatoryElevationMode, ElevationMode }
import pl.combosolutions.backup.{ Logging, Cleaner, ReportException }
import pl.combosolutions.backup.tasks.DependencyType._
import pl.combosolutions.backup.tasks.TasksExceptionMessages._

class TaskBuilder[BR, PBR, CBR, RR, PRR, CRR](
    private[tasks] val backupSubTaskBuilder:  SubTaskBuilder[BR, PBR, CBR],
    private[tasks] val restoreSubTaskBuilder: SubTaskBuilder[RR, PRR, CRR]
) extends Logging {

  type BackupResult = BR
  type ParentBackupResult = PBR
  type ChildBackupResult = CBR
  type RestoreResult = RR
  type ParentRestoreResult = PRR
  type ChildRestoreResult = CRR

  type BackupSubTaskT = SubTask[BR]
  type BackupSubTaskBuilderT = SubTaskBuilder[BR, PBR, CBR]

  type RestoreSubTaskT = SubTask[RR]
  type RestoreSubTaskBuilderT = SubTaskBuilder[RR, PRR, CRR]

  type TaskT = Task[BackupResult, RestoreResult]
  type TaskBuilderT = TaskBuilder[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult]
  type TaskConfigT = TaskConfig[BackupResult, ParentBackupResult, ChildBackupResult, RestoreResult, ParentRestoreResult, ChildRestoreResult]

  type ParentTaskBuilderT = TaskBuilder[ParentBackupResult, _, BackupResult, ParentRestoreResult, _, RestoreResult]

  type ChildTaskBuilderT = TaskBuilder[ChildBackupResult, BackupResult, _, ChildRestoreResult, RestoreResult, _]

  type ConfiguratorT = Configurator[BR, PBR, CBR, RR, PRR, CRR]

  private val task = new TaskT(backupSubTaskBuilder.injectableProxy.get, restoreSubTaskBuilder.injectableProxy.get)

  lazy val build: TaskT = {

    logger debug s"Run builder $getClass"

    def parent = config.parent getOrElse (ReportException onIllegalStateOf ParentDependentWithoutParent)
    val children = config.children.toList
    def backupResult(child: ChildTaskBuilderT): Propagator = child.backupSubTaskBuilder.injectableProxy.get.propagator
    def restoreResult(child: ChildTaskBuilderT): Propagator = child.restoreSubTaskBuilder.injectableProxy.get.propagator

    children foreach (_.build)

    backupSubTaskBuilder.injectableProxy.dependencyType match {
      case Independent => backupSubTaskBuilder configurePropagation (children.toSet.map(backupResult))
      case ParentDependent =>
        backupSubTaskBuilder configureForParent parent.backupSubTaskBuilder
        backupSubTaskBuilder configurePropagation (children.toSet.map(backupResult))
      case ChildDependent => backupSubTaskBuilder configureForChildren (children map (_.backupSubTaskBuilder))
    }

    restoreSubTaskBuilder.injectableProxy.dependencyType match {
      case Independent => restoreSubTaskBuilder configurePropagation (children.toSet.map(restoreResult))
      case ParentDependent =>
        restoreSubTaskBuilder configureForParent parent.restoreSubTaskBuilder
        restoreSubTaskBuilder configurePropagation (children.toSet.map(backupResult))
      case ChildDependent => restoreSubTaskBuilder configureForChildren (children map (_.restoreSubTaskBuilder))
    }

    logger trace s"Builder $getClass finished"

    task
  }

  private val settingsProxy: MutableSettings = MutableSettings(None)
  val withSettings: Settings = settingsProxy
  def updateSettings(settings: Settings): Unit = settingsProxy.innerSettings = Some(Settings(settings))
  def updateSettings(
    cleaner:                 Cleaner                 = settingsProxy.cleaner,
    withElevation:           ElevationMode           = settingsProxy.withElevation,
    withObligatoryElevation: ObligatoryElevationMode = settingsProxy.withObligatoryElevation,
    components:              ComponentRegistry       = settingsProxy.components,
    backupDir:               Path                    = settingsProxy.backupDir,
    copyOptions:             Array[CopyOption]       = settingsProxy.copyOptions
  ): Unit = {
    settingsProxy.innerSettings = Some(ImmutableSettings(
      cleaner                 = cleaner,
      withElevation           = withElevation,
      withObligatoryElevation = withObligatoryElevation,
      components              = components,
      backupDir               = backupDir,
      copyOptions             = copyOptions
    ))
  }

  private var config: TaskConfigT = new TaskConfigT
  private[tasks] def setParent(parent: ParentTaskBuilderT): Unit = config = config setParent parent
  private[tasks] def addChild(child: ChildTaskBuilderT): Unit = config = config addChild child
}
