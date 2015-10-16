package pl.combosolutions.backup.tasks

import java.nio.file.{ CopyOption, Path }

import pl.combosolutions.backup.psm.ComponentRegistry
import pl.combosolutions.backup.psm.elevation.{ ElevationMode, ObligatoryElevationMode }

import scala.collection.mutable

abstract class MutableConfigurator[BR, PBR, CBR, RR, PRR, CRR](
    protected val parentOpt:       Option[MutableConfigurator[PBR, _, BR, PRR, _, RR]],
    protected val initialSettings: Settings
) {

  type TaskBuilderT = TaskBuilder[BR, PBR, CBR, RR, PRR, CRR]
  type TaskT = TaskBuilderT#TaskT
  type TaskConfigT = TaskBuilderT#TaskConfigT

  type ChildConfiguratorT = MutableConfigurator[CBR, BR, _, CRR, RR, _]

  protected val children: mutable.MutableList[ChildConfiguratorT] = mutable.MutableList()

  protected var config: TaskConfigT = taskBuilder.newConfig
  parentOpt foreach { parent =>
    config = config setParent parent.taskBuilder
    parent addChild this
  }

  protected var settings: ImmutableSettings = Settings(initialSettings)
  protected val settingsProxy: MutableSettings = MutableSettings(settings)

  protected def adjustForParent[PB <: PBR, PR <: PRR] = asInstanceOf[MutableConfigurator[BR, PB, CBR, RR, PR, CRR]]

  protected def adjustForChildren[CB <: CBR, CR <: CRR] = asInstanceOf[MutableConfigurator[BR, PBR, CB, RR, PRR, CR]]

  protected def taskBuilder: TaskBuilder[BR, PBR, CBR, RR, PRR, CRR]

  protected def build = taskBuilder buildFor config

  def updateSettings(
    withElevation:           ElevationMode           = settings.withElevation,
    withObligatoryElevation: ObligatoryElevationMode = settings.withObligatoryElevation,
    components:              ComponentRegistry       = settings.components,
    backupDir:               Path                    = settings.backupDir,
    copyOptions:             Array[CopyOption]       = settings.copyOptions
  ): Unit = {
    settings = settings.copy(
      withElevation           = withElevation,
      withObligatoryElevation = withObligatoryElevation,
      components              = components,
      backupDir               = backupDir,
      copyOptions             = copyOptions
    )
    settingsProxy.innerSettings = settingsProxy
  }

  private def addChild(child: ChildConfiguratorT): Unit = {
    children += child
    config = config addChild child.taskBuilder
  }
}
