package pl.combosolutions.backup.tasks

import pl.combosolutions.backup._
import pl.combosolutions.backup.tasks.DependencyType._
import pl.combosolutions.backup.tasks.TasksExceptionMessages._

sealed abstract class SubTaskBuilder[R, PR, CR](
    dependencyType: DependencyType
) extends SType[R, PR, CR] {

  val injectableProxy = new SubTaskProxy[Result](dependencyType)

  def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit

  def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit
}

final class FakeSubTaskBuilder[R, PR, CR](
    subTask:        SubTask[R],
    dependencyType: DependencyType
) extends SubTaskBuilder[R, PR, CR](dependencyType) {

  injectableProxy setImplementation subTask

  def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit =
    ReportException onIllegalArgumentOf FakeBuilderWithConfig

  def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    ReportException onIllegalArgumentOf FakeBuilderWithConfig
}

case class IndependentSubTaskBuilder[R, PR, CR](
    action: () => Async[R]
) extends SubTaskBuilder[R, PR, CR](Independent) {

  injectableProxy.setImplementation(new IndependentSubTask[Result](action))

  final override def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit =
    ReportException onIllegalArgumentOf IndependentTaskWithParentConfig

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    ReportException onIllegalArgumentOf IndependentTaskWithChildrenConfig
}

case class ParentDependentSubTaskBuilder[R, PR, CR](
    action: Function[PR, Async[R]]
) extends SubTaskBuilder[R, PR, CR](ParentDependent) {

  final override def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit = {
    assert(parentTask.injectableProxy.dependencyType != ChildDependent, CircularDependency)
    injectableProxy.setImplementation(new ParentDependentSubTaskT(action, parentTask.injectableProxy))
  }

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    ReportException onIllegalArgumentOf ParentDependentWithChildrenConfig
}

case class ChildDependentSubTaskBuilder[R, PR, CR](
    action: Function[Traversable[CR], Async[R]]
) extends SubTaskBuilder[R, PR, CR](ChildDependent) {

  final override def configureForParent(childrenTasks: SubTaskBuilder[ParentResult, _, _]): Unit =
    ReportException onIllegalArgumentOf ChildrenDependentWithParentConfig

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit = {
    assert(childrenTasks.forall(_.injectableProxy.dependencyType != ParentDependent), CircularDependency)
    injectableProxy.setImplementation(new ChildDependentSubTaskT(action, childrenTasks.map(_.injectableProxy)))
  }
}
