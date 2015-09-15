package pl.combosolutions.backup.dsl.tasks.beta

import pl.combosolutions.backup.dsl.internals.ExecutionContexts

import scala.concurrent.{ ExecutionContext, Future }

import SubTask._

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

object SubTask {

  def compose[Result](children: Traversable[SubTask[Result]])(implicit executor: ExecutionContext): Future[Traversable[Result]] =
    Future sequence (children map (_.result))
}

final class SubTaskProxy[Result](proxyDependencyType: DependencyType.Value) extends SubTask[Result] {

  val dependencyType = proxyDependencyType

  private var implementation: Option[SubTask[Result]] = None

  def setImplementation(subTask: SubTask[Result]): Unit = {
    if (implementation.isEmpty) implementation = Some(subTask)
    else throw new IllegalStateException("Implementation is already set")
  }

  def execute = implementation map (_.result) getOrElse (throw new IllegalStateException("Implementation not set"))
}

class SubTaskBuilder[Result](dependencyType: DependencyType.Value) {

  val injectableProxy = new SubTaskProxy[Result](dependencyType)
}

abstract class IndependentSubTask[Result] extends SubTask[Result] {

  val dependencyType = DependencyType.Independent
}

abstract class ParentDependentSubTask[Result, ParentResult](parent: SubTask[ParentResult]) extends SubTask[Result] {

  val dependencyType = DependencyType.ParentDependent

  type Behavior = Function[ParentResult, Future[Result]]

  final protected def execute = parent.result flatMap (executeWithParentResult(_))

  protected val executeWithParentResult: Behavior
}

abstract class ChildDependentSubTask[Result, ChildResult](children: Traversable[SubTask[ChildResult]]) extends SubTask[Result] {

  val dependencyType = DependencyType.ChildDependent

  type Behavior = Function[Traversable[ChildResult], Future[Result]]

  final protected def execute = compose(children) flatMap (executeWithChildrenResults(_))

  protected val executeWithChildrenResults: Behavior
}

