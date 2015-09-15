package pl.combosolutions.backup.dsl.tasks.beta

import pl.combosolutions.backup.dsl.ReportException
import pl.combosolutions.backup.dsl.internals.ExecutionContexts
import pl.combosolutions.backup.dsl.tasks.beta.TasksExceptionMessages._

import scala.concurrent.{ ExecutionContext, Future }

// Dependency types

object DependencyType extends Enumeration {
  type DependencyType = Value
  val Independent, ParentDependent, ChildDependent = Value
}

// SubTasks

sealed trait SubTask[Result] {

  implicit val context: ExecutionContext = ExecutionContexts.Task.context

  val dependencyType: DependencyType.Value

  lazy val result: Future[Result] = execute

  protected def execute: Future[Result]

  private lazy val fakeSelfBuilder = new FakeSubTaskBuilder[Result, Nothing, Nothing](this, dependencyType)

  def flatMap[MappedResult](mapping: Result => SubTask[MappedResult]): SubTask[MappedResult] = {
    val flattenMapping = (result: Result) => mapping(result).result
    SubTask(flattenMapping, this)
  }

  def map[MappedResult](mapping: Result => MappedResult): SubTask[MappedResult] = {
    val futureMapping = (result: Result) => Future { mapping(result) }
    SubTask(futureMapping, this)
  }
}

object SubTask {

  def apply[Result](action: () => Future[Result]): SubTask[Result] =
    new IndependentSubTaskBuilder(action).injectableProxy

  def apply[Result, ParentResult](action: ParentResult => Future[Result], parent: SubTask[ParentResult]): SubTask[Result] = {
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

final class IndependentSubTask[Result](action: () => Future[Result]) extends SubTask[Result] {

  val dependencyType = DependencyType.Independent

  final override def execute = action()
}

case class IndependentSubTaskBuilder[Result, ParentResult, ChildResult](action: () => Future[Result]) extends SubTaskBuilder[Result, ParentResult, ChildResult](DependencyType.Independent) {

  injectableProxy.setImplementation(new IndependentSubTask[Result](action))

  final override def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit =
    ReportException onIllegalStateOf IndependentTaskWithParentConfig

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    ReportException onIllegalStateOf IndependentTaskWithChildrenConfig
}

// Parent dependent subtasks

final class ParentDependentSubTask[Result, ParentResult](action: Function[ParentResult, Future[Result]], parent: SubTask[ParentResult]) extends SubTask[Result] {

  val dependencyType = DependencyType.ParentDependent

  type Behavior = Function[ParentResult, Future[Result]]

  final protected def execute = parent.result flatMap (executeWithParentResult(_))

  final val executeWithParentResult: Behavior = action
}

case class ParentDependentSubTaskBuilder[Result, ParentResult, ChildResult](action: Function[ParentResult, Future[Result]]) extends SubTaskBuilder[Result, ParentResult, ChildResult](DependencyType.ParentDependent) {

  final override def configureForParent(parentTask: SubTaskBuilder[ParentResult, _, _]): Unit = {
    assert(parentTask.injectableProxy.dependencyType != DependencyType.ChildDependent, CircularDependency)
    injectableProxy.setImplementation(new ParentDependentSubTask[Result, ParentResult](action, parentTask.injectableProxy))
  }

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit =
    ReportException onIllegalStateOf ParentDependentWithChildrenConfig
}

// Child dependent subtasks

object ChildDependentSubTask {

  def compose[Result](children: Traversable[SubTask[Result]])(implicit executor: ExecutionContext): Future[Traversable[Result]] =
    Future sequence (children map (_.result))
}

final class ChildDependentSubTask[Result, ChildResult](action: Function[Traversable[ChildResult], Future[Result]], children: Traversable[SubTask[ChildResult]]) extends SubTask[Result] {

  import ChildDependentSubTask._

  val dependencyType = DependencyType.ChildDependent

  type Behavior = Function[Traversable[ChildResult], Future[Result]]

  final protected def execute = compose(children) flatMap (executeWithChildrenResults(_))

  final val executeWithChildrenResults: Behavior = action
}

case class ChildDependentSubTaskBuilder[Result, ParentResult, ChildResult](action: Function[Traversable[ChildResult], Future[Result]]) extends SubTaskBuilder[Result, ParentResult, ChildResult](DependencyType.ChildDependent) {

  final override def configureForParent(childrenTasks: SubTaskBuilder[ParentResult, _, _]): Unit =
    ReportException onIllegalStateOf ChildrenDependentWithParentConfig

  final override def configureForChildren(childrenTasks: Traversable[SubTaskBuilder[ChildResult, _, _]]): Unit = {
    assert(childrenTasks.forall(_.injectableProxy.dependencyType != DependencyType.ParentDependent), CircularDependency)
    injectableProxy.setImplementation(new ChildDependentSubTask[Result, ChildResult](action, childrenTasks.map(_.injectableProxy)))
  }
}
