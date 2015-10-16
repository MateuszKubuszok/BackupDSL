package pl.combosolutions.backup.tasks

import scala.collection.mutable

abstract class Configurator[BR, PBR, CBR, RR, PRR, CRR](
    protected val parentOpt:       Option[Configurator[PBR, _, BR, PRR, _, RR]],
    protected val initialSettings: Settings
) {

  type TaskBuilderT = TaskBuilder[BR, PBR, CBR, RR, PRR, CRR]
  type TaskT = TaskBuilderT#TaskT
  type TaskConfigT = TaskBuilderT#TaskConfigT

  type ParentTaskBuilderT = TaskBuilderT#ParentTaskBuilderT
  type ParentTaskConfiguratorT = ParentTaskBuilderT#ConfiguratorT

  type ChildTaskBuilderT = TaskBuilderT#ChildTaskBuilderT
  type ChildTaskConfiguratorT = ChildTaskBuilderT#ConfiguratorT

  parentOpt foreach (_ addChild this.asInstanceOf[ParentTaskConfiguratorT#ChildTaskConfiguratorT])

  def builder: TaskBuilderT

  private val children: mutable.MutableList[ChildTaskConfiguratorT] = mutable.MutableList()

  protected def adjustForParent[PB <: PBR, PR <: PRR] = asInstanceOf[Configurator[BR, PB, CBR, RR, PR, CRR]]

  protected def adjustForChildren[CB <: CBR, CR <: CRR] = asInstanceOf[Configurator[BR, PBR, CB, RR, PRR, CR]]

  private lazy val configure: Unit = {
    parentOpt foreach { parent =>
      builder setParent parent.builder
    }
    children foreach { child =>
      child.configure
      builder addChild child.builder
    }
  }

  private lazy val build: TaskT = {
    children foreach (_.build)
    builder.build
  }

  protected lazy val buildAll: TaskT = {
    configure
    build
  }

  private[tasks] def addChild(child: ChildTaskConfiguratorT): Unit = {
    children += child
  }
}
