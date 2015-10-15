package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.tasks.DependencyType._
import pl.combosolutions.backup.tasks.TasksExceptionMessages._

class TaskBuilder[BR, PBR, CBR, RR, PRR, CRR](
    private[tasks] val backupSubTaskBuilder:  SubTaskBuilder[BR, PBR, CBR],
    private[tasks] val restoreSubTaskBuilder: SubTaskBuilder[RR, PRR, CRR]
) extends TType[BR, PBR, CBR, RR, PRR, CRR] {

  private val task = new TaskT(backupSubTaskBuilder.injectableProxy, restoreSubTaskBuilder.injectableProxy)

  def build(taskConfig: TaskConfigT): TaskT = {

    def parent = taskConfig.parent getOrElse (ReportException onIllegalStateOf ParentDependentWithoutParent)
    def children = taskConfig.children

    backupSubTaskBuilder.injectableProxy.dependencyType match {
      case ParentDependent => backupSubTaskBuilder configureForParent parent.backupSubTaskBuilder
      case ChildDependent  => backupSubTaskBuilder configureForChildren (children map (_.backupSubTaskBuilder))
    }

    restoreSubTaskBuilder.injectableProxy.dependencyType match {
      case ParentDependent => restoreSubTaskBuilder configureForParent parent.restoreSubTaskBuilder
      case ChildDependent  => restoreSubTaskBuilder configureForChildren (children map (_.restoreSubTaskBuilder))
    }

    task
  }
}
