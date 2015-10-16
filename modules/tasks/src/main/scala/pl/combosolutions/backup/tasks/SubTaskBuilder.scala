package pl.combosolutions.backup.tasks

import pl.combosolutions.backup._
import pl.combosolutions.backup.tasks.DependencyType._
import pl.combosolutions.backup.tasks.TasksExceptionMessages._

sealed abstract class SubTaskBuilder[R, PR, CR](
    dependencyType: DependencyType
) {

  type Result = R
  type ParentResult = PR
  type ChildResult = CR

  type SubTaskT = SubTask[Result]
  type SubTaskBuilderT = SubTaskBuilder[Result, ParentResult, ChildResult]

  type ParentSubTaskBuilderT = SubTaskBuilder[ParentResult, _, _]

  type ChildSubTaskBuilderT = SubTaskBuilder[ChildResult, _, _]

  type ParentDependentSubTaskT = ParentDependentSubTask[Result, ParentResult]
  type ChildDependentSubTaskT = ChildDependentSubTask[Result, ChildResult]

  val injectableProxy = new SubTaskProxy[Result](dependencyType)

  def configureForParent(parentTask: ParentSubTaskBuilderT): Unit

  def configureForChildren(childrenTasks: Traversable[ChildSubTaskBuilderT]): Unit
}

final class FakeSubTaskBuilder[R, PR, CR](
    subTask:        SubTask[R],
    dependencyType: DependencyType
) extends SubTaskBuilder[R, PR, CR](dependencyType) {

  injectableProxy setImplementation subTask

  def configureForParent(parentTask: ParentSubTaskBuilderT): Unit =
    ReportException onIllegalArgumentOf FakeBuilderWithConfig

  def configureForChildren(childrenTasks: Traversable[ChildSubTaskBuilderT]): Unit =
    ReportException onIllegalArgumentOf FakeBuilderWithConfig
}

case class IndependentSubTaskBuilder[R, PR, CR](
    action: () => Async[R]
) extends SubTaskBuilder[R, PR, CR](Independent) {

  injectableProxy.setImplementation(new IndependentSubTask[Result](action))

  final override def configureForParent(parentTask: ParentSubTaskBuilderT): Unit =
    ReportException onIllegalArgumentOf IndependentTaskWithParentConfig

  final override def configureForChildren(childrenTasks: Traversable[ChildSubTaskBuilderT]): Unit =
    ReportException onIllegalArgumentOf IndependentTaskWithChildrenConfig
}

case class ParentDependentSubTaskBuilder[R, PR, CR](
    action: Function[PR, Async[R]]
) extends SubTaskBuilder[R, PR, CR](ParentDependent) {

  final override def configureForParent(parentTask: ParentSubTaskBuilderT): Unit = {
    assert(parentTask.injectableProxy.dependencyType != ChildDependent, CircularDependency)
    injectableProxy.setImplementation(new ParentDependentSubTaskT(action, parentTask.injectableProxy))
  }

  final override def configureForChildren(childrenTasks: Traversable[ChildSubTaskBuilderT]): Unit =
    ReportException onIllegalArgumentOf ParentDependentWithChildrenConfig
}

case class ChildDependentSubTaskBuilder[R, PR, CR](
    action: Function[Traversable[CR], Async[R]]
) extends SubTaskBuilder[R, PR, CR](ChildDependent) {

  final override def configureForParent(childrenTasks: ParentSubTaskBuilderT): Unit =
    ReportException onIllegalArgumentOf ChildrenDependentWithParentConfig

  final override def configureForChildren(childrenTasks: Traversable[ChildSubTaskBuilderT]): Unit = {
    assert(childrenTasks.forall(_.injectableProxy.dependencyType != ParentDependent), CircularDependency)
    injectableProxy.setImplementation(new ChildDependentSubTaskT(action, childrenTasks.map(_.injectableProxy)))
  }
}
