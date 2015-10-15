package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.{ Async, AsyncTransformer }
import pl.combosolutions.backup.psm.ExecutionContexts
import pl.combosolutions.backup.tasks.DependencyType._
import pl.combosolutions.backup.tasks.TasksExceptionMessages._

import scala.concurrent.ExecutionContext

sealed trait SubTask[Result] {

  implicit val context: ExecutionContext = ExecutionContexts.Task.context

  val dependencyType: DependencyType

  lazy val result: Async[Result] = execute

  protected def execute: Async[Result]

  private lazy val fakeSelfBuilder = new FakeSubTaskBuilder[Result, Nothing, Nothing](this, dependencyType)

  def flatMap[MappedResult](mapping: Result => SubTask[MappedResult]): SubTask[MappedResult] = {
    val flattenMapping = (result: Result) => mapping(result).result
    SubTask(flattenMapping, this)
  }

  def map[MappedResult](mapping: Result => MappedResult): SubTask[MappedResult] = {
    val futureMapping = (result: Result) => Async some mapping(result)
    SubTask(futureMapping, this)
  }
}

object SubTask {

  def apply[Result](action: () => Async[Result]): SubTask[Result] =
    new IndependentSubTaskBuilder(action).injectableProxy

  def apply[Result, ParentResult](action: ParentResult => Async[Result], parent: SubTask[ParentResult]): SubTask[Result] = {
    val builder = new ParentDependentSubTaskBuilder[Result, ParentResult, Nothing](action)
    builder configureForParent parent.fakeSelfBuilder
    builder.injectableProxy
  }

  implicit def proxyToSubTask[Result](proxy: SubTaskProxy[Result]): SubTask[Result] = proxy
}

final class SubTaskProxy[Result](proxyDependencyType: DependencyType) extends SubTask[Result] {

  val dependencyType = proxyDependencyType

  private var implementation: Option[SubTask[Result]] = None

  private[tasks] def setImplementation[T <: SubTask[Result]](subTask: T): Unit = synchronized {
    assert(implementation.isEmpty, ProxyInitialized)
    require(dependencyType == subTask.dependencyType, CircularDependency)

    implementation = Some(subTask)
  }

  override def execute = synchronized {
    assert(implementation.isDefined, ProxyNotInitialized)
    implementation.get.result
  }
}

final class IndependentSubTask[Result](action: () => Async[Result]) extends SubTask[Result] {

  val dependencyType = Independent

  override def execute = action()
}

final class ParentDependentSubTask[Result, ParentResult](
    action: Function[ParentResult, Async[Result]],
    parent: SubTask[ParentResult]
) extends SubTask[Result] {

  val dependencyType = ParentDependent

  type Behavior = Function[ParentResult, Async[Result]]

  protected def execute = parent.result.asAsync flatMap (executeWithParentResult(_))

  val executeWithParentResult: Behavior = action
}

object ChildDependentSubTask {

  def compose[Result](children: Traversable[SubTask[Result]])(implicit executor: ExecutionContext): Async[Traversable[Result]] =
    Async completeSequence (children map (_.result))
}

final class ChildDependentSubTask[Result, ChildResult](
    action:   Function[Traversable[ChildResult], Async[Result]],
    children: Traversable[SubTask[ChildResult]]
) extends SubTask[Result] {

  import ChildDependentSubTask._

  val dependencyType = ChildDependent

  type Behavior = Function[Traversable[ChildResult], Async[Result]]

  protected def execute = compose(children).asAsync flatMap (executeWithChildrenResults(_))

  val executeWithChildrenResults: Behavior = action
}

// TestSubTask

private[tasks] trait TestSubTask[Result] extends SubTask[Result]
