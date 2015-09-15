package pl.combosolutions.backup.dsl.tasks.beta

import pl.combosolutions.backup.dsl.internals.ExecutionContexts

import scala.concurrent.{ ExecutionContext, Future }

import ChildDependentSubTask._

object DependencyType extends Enumeration {
  type DependencyType = Value
  val Independent, ParentDependent, ChildDependent = Value
}

sealed trait SubTask[Result] {

  implicit val context: ExecutionContext = ExecutionContexts.Task.context

  val dependencyType: DependencyType.Value

  lazy val result: Future[Result] = execute

  protected def execute: Future[Result]
}

final class SubTaskProxy[Result](proxyDependencyType: DependencyType.Value) extends SubTask[Result] {

  val dependencyType = proxyDependencyType

  private var implementation: Option[SubTask[Result]] = None

  def setImplementation(subTask: SubTask[Result]): Unit = {
    assert(implementation.isEmpty, "Implementation is already set")
    assert(dependencyType == subTask.dependencyType, "Declared dependency type must match the actual one")

    implementation = Some(subTask)
  }

  def execute = implementation map (_.result) getOrElse (throw new IllegalStateException("Implementation not set"))
}

sealed abstract class SubTaskBuilder[Result, ParentResult, ChildResult](dependencyType: DependencyType.Value) {

  val injectableProxy = new SubTaskProxy[Result](dependencyType)

  def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit

  def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit
}

// Independent subtasks

final class IndependentSubTask[Result](action: () => Future[Result]) extends SubTask[Result] {

  val dependencyType = DependencyType.Independent

  final override def execute = action()
}

class IndependentSubTaskBuilder[Result, ParentResult, ChildResult](action: () => Future[Result]) extends SubTaskBuilder[Result, ParentResult, ChildResult](DependencyType.Independent) {

  injectableProxy.setImplementation(new IndependentSubTask[Result](action))

  final override def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit =
    throw new IllegalStateException("Independent task cannot rely on any parent task")

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    throw new IllegalStateException("Independent task cannot rely on any children tasks")
}

// Parent dependent subtasks

final class ParentDependentSubTask[Result, ParentResult](action: Function[ParentResult, Future[Result]], parent: SubTask[ParentResult]) extends SubTask[Result] {

  val dependencyType = DependencyType.ParentDependent

  type Behavior = Function[ParentResult, Future[Result]]

  final protected def execute = parent.result flatMap (executeWithParentResult(_))

  final val executeWithParentResult: Behavior = action
}

class ParentDependentSubTaskBuilder[Result, ParentResult, ChildResult](action: Function[ParentResult, Future[Result]]) extends SubTaskBuilder[Result, ParentResult, ChildResult](DependencyType.ParentDependent) {

  final override def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit = {
    assert(parentTask.injectableProxy.dependencyType != DependencyType.ChildDependent, "Circular dependency is not allowed")
    injectableProxy.setImplementation(new ParentDependentSubTask[Result, ParentResult](action, parentTask.injectableProxy))
  }

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    throw new IllegalStateException("Parent dependent task cannot rely on any children tasks")
}

// Child dependent subtasks

object ChildDependentSubTask {

  def compose[Result](children: Traversable[SubTask[Result]])(implicit executor: ExecutionContext): Future[Traversable[Result]] =
    Future sequence (children map (_.result))
}

final class ChildDependentSubTask[Result, ChildResult](action: Function[Traversable[ChildResult], Future[Result]], children: Traversable[SubTask[ChildResult]]) extends SubTask[Result] {

  val dependencyType = DependencyType.ChildDependent

  type Behavior = Function[Traversable[ChildResult], Future[Result]]

  final protected def execute = compose(children) flatMap (executeWithChildrenResults(_))

  final val executeWithChildrenResults: Behavior = action
}

class ChildDependentSubTaskBuilder[Result, ParentResult, ChildResult](action: Function[Traversable[ChildResult], Future[Result]]) extends SubTaskBuilder[Result, ParentResult, ChildResult](DependencyType.ChildDependent) {

  final override def configureForParent(childrenTasks: SubTaskBuilder[ParentResult, _, _]): Unit =
    throw new IllegalStateException("Child dependent task cannot rely on any parent task")

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit = {
    assert(childrenTasks.forall(_.injectableProxy.dependencyType != DependencyType.ParentDependent), "Circular dependency is not allowed")
    injectableProxy.setImplementation(new ChildDependentSubTask[Result, ChildResult](action, childrenTasks.map(_.injectableProxy)))
  }
}
