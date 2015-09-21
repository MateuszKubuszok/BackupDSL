package pl.combosolutions.backup.dsl.tasks.beta

import pl.combosolutions.backup.{ AsyncResult, ReportException }
import pl.combosolutions.backup.wrapAsyncResultForMapping
import pl.combosolutions.backup.dsl.tasks.beta.TasksExceptionMessages._
import pl.combosolutions.backup.psm.ExecutionContexts

import scala.concurrent.ExecutionContext

// Dependency types

object DependencyType extends Enumeration {
  type DependencyType = Value
  val Independent, ParentDependent, ChildDependent = Value
}

// SubTasks

sealed trait SubTask[Result] {

  implicit val context: ExecutionContext = ExecutionContexts.Task.context

  val dependencyType: DependencyType.Value

  lazy val result: AsyncResult[Result] = execute

  protected def execute: AsyncResult[Result]

  private lazy val fakeSelfBuilder = new FakeSubTaskBuilder[Result, Nothing, Nothing](this, dependencyType)

  def flatMap[MappedResult](mapping: Result => SubTask[MappedResult]): SubTask[MappedResult] = {
    val flattenMapping = (result: Result) => mapping(result).result
    SubTask(flattenMapping, this)
  }

  def map[MappedResult](mapping: Result => MappedResult): SubTask[MappedResult] = {
    val futureMapping = (result: Result) => AsyncResult { mapping(result) }
    SubTask(futureMapping, this)
  }
}

object SubTask {

  def apply[Result](action: () => AsyncResult[Result]): SubTask[Result] =
    new IndependentSubTaskBuilder(action).injectableProxy

  def apply[Result, ParentResult](action: ParentResult => AsyncResult[Result], parent: SubTask[ParentResult]): SubTask[Result] = {
    val builder = new ParentDependentSubTaskBuilder[Result, ParentResult, Nothing](action)
    builder configureForParent parent.fakeSelfBuilder
    builder.injectableProxy
  }

  implicit def proxyToSubTask[Result](proxy: SubTaskProxy[Result]): SubTask[Result] = proxy
}

// SubTaskBuilders

final class SubTaskProxy[Result](proxyDependencyType: DependencyType.Value) extends SubTask[Result] {

  val dependencyType = proxyDependencyType

  private var implementation: Option[SubTask[Result]] = None

  private[beta] def setImplementation[T <: SubTask[Result]](subTask: T): Unit = {
    assert(implementation.isEmpty, ProxyInitialized)
    assert(dependencyType == subTask.dependencyType, CircularDependency)

    implementation = Some(subTask)
  }

  def execute = implementation map (_.result) getOrElse (ReportException onIllegalStateOf ProxyNotInitialized)
}

sealed abstract class SubTaskBuilder[Result, ParentResult, ChildResult](dependencyType: DependencyType.Value) {

  val injectableProxy = new SubTaskProxy[Result](dependencyType)

  def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit

  def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit
}

final class FakeSubTaskBuilder[Result, ParentResult, ChildResult](subTask: SubTask[Result], dependencyType: DependencyType.Value) extends SubTaskBuilder[Result, ParentResult, ChildResult](dependencyType) {

  injectableProxy setImplementation subTask

  def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit =
    ReportException onIllegalStateOf FakeBuilderWithConfig

  def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    ReportException onIllegalStateOf FakeBuilderWithConfig
}

// Independent subtasks

final class IndependentSubTask[Result](action: () => AsyncResult[Result]) extends SubTask[Result] {

  val dependencyType = DependencyType.Independent

  final override def execute = action()
}

case class IndependentSubTaskBuilder[Result, ParentResult, ChildResult](action: () => AsyncResult[Result]) extends SubTaskBuilder[Result, ParentResult, ChildResult](DependencyType.Independent) {

  injectableProxy.setImplementation(new IndependentSubTask[Result](action))

  final override def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit =
    ReportException onIllegalStateOf IndependentTaskWithParentConfig

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    ReportException onIllegalStateOf IndependentTaskWithChildrenConfig
}

// Parent dependent subtasks

final class ParentDependentSubTask[Result, ParentResult](action: Function[ParentResult, AsyncResult[Result]], parent: SubTask[ParentResult]) extends SubTask[Result] {

  val dependencyType = DependencyType.ParentDependent

  type Behavior = Function[ParentResult, AsyncResult[Result]]

  final protected def execute = parent.result.asAsync flatMap (executeWithParentResult(_))

  final val executeWithParentResult: Behavior = action
}

case class ParentDependentSubTaskBuilder[Result, ParentResult, ChildResult](action: Function[ParentResult, AsyncResult[Result]]) extends SubTaskBuilder[Result, ParentResult, ChildResult](DependencyType.ParentDependent) {

  final override def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit = {
    assert(parentTask.injectableProxy.dependencyType != DependencyType.ChildDependent, CircularDependency)
    injectableProxy.setImplementation(new ParentDependentSubTask[Result, ParentResult](action, parentTask.injectableProxy))
  }

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    ReportException onIllegalStateOf ParentDependentWithChildrenConfig
}

// Child dependent subtasks

object ChildDependentSubTask {

  def compose[Result](children: Traversable[SubTask[Result]])(implicit executor: ExecutionContext): AsyncResult[Traversable[Result]] =
    AsyncResult completeSequence (children map (_.result))
}

final class ChildDependentSubTask[Result, ChildResult](action: Function[Traversable[ChildResult], AsyncResult[Result]], children: Traversable[SubTask[ChildResult]]) extends SubTask[Result] {

  import ChildDependentSubTask._

  val dependencyType = DependencyType.ChildDependent

  type Behavior = Function[Traversable[ChildResult], AsyncResult[Result]]

  final protected def execute = compose(children).asAsync flatMap (executeWithChildrenResults(_))

  final val executeWithChildrenResults: Behavior = action
}

case class ChildDependentSubTaskBuilder[Result, ParentResult, ChildResult](action: Function[Traversable[ChildResult], AsyncResult[Result]]) extends SubTaskBuilder[Result, ParentResult, ChildResult](DependencyType.ChildDependent) {

  final override def configureForParent(childrenTasks: SubTaskBuilder[ParentResult, _, _]): Unit =
    ReportException onIllegalStateOf ChildrenDependentWithParentConfig

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit = {
    assert(childrenTasks.forall(_.injectableProxy.dependencyType != DependencyType.ParentDependent), CircularDependency)
    injectableProxy.setImplementation(new ChildDependentSubTask[Result, ChildResult](action, childrenTasks.map(_.injectableProxy)))
  }
}
