package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.{ Logging, ExecutionContexts, Async, AsyncTransformer }
import pl.combosolutions.backup.tasks.DependencyType._
import pl.combosolutions.backup.tasks.TasksExceptionMessages._

import scala.collection.mutable
import scala.concurrent.{ blocking, ExecutionContext, Future }

sealed trait SubTask[Result] extends Logging {

  implicit val context: ExecutionContext = ExecutionContexts.Task.context

  val dependencyType: DependencyType

  private val propagation: mutable.Set[() => Unit] = mutable.Set()
  private[tasks] def getPropagation = propagation

  lazy val result: Async[Result] = {
    Future {
      blocking {
        if (propagation.nonEmpty) {
          logger trace s"Poking each dependant future to make sure all are called (size=${propagation.size})"
          propagation foreach { notified =>
            logger trace s"Poke start"
            notified()
            logger trace s"Poke stop"
          }
          logger trace s"Child futures poked"
        }
      }
    }
    execute
  }

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
    new IndependentSubTaskBuilder(action).injectableProxy.get

  def apply[Result, ParentResult](action: ParentResult => Async[Result], parent: SubTask[ParentResult]): SubTask[Result] = {
    val builder = new ParentDependentSubTaskBuilder[Result, ParentResult, Nothing](action)
    builder configureForParent parent.fakeSelfBuilder
    builder.injectableProxy.get
  }

  implicit def proxyToSubTask[Result](proxy: SubTaskProxy[Result]): SubTask[Result] = proxy
}

final class SubTaskProxy[Result](proxyDependencyType: DependencyType) extends SubTask[Result] {

  val dependencyType = proxyDependencyType

  def get: SubTask[Result] = implementation getOrElse this

  private var implementation: Option[SubTask[Result]] = None

  private val propagation: mutable.Set[() => Unit] = mutable.Set()
  private[tasks] override def getPropagation = {
    if (implementation.isDefined) implementation.get.getPropagation
    else propagation
  }

  private[tasks] def setImplementation[T <: SubTask[Result]](subTask: T): Unit = {
    assert(implementation.isEmpty, ProxyInitialized)
    require(dependencyType == subTask.dependencyType, CircularDependency)
    subTask.getPropagation ++= propagation
    implementation = Some(subTask)
  }

  override lazy val result = execute

  override lazy val execute = {
    assert(implementation.isDefined, ProxyNotInitialized)
    implementation.get.result
  }
}

final class IndependentSubTask[Result](action: () => Async[Result]) extends SubTask[Result] {

  val dependencyType = Independent

  override def execute = {
    action()
  }
}

final class ParentDependentSubTask[Result, ParentResult](
    action: Function[ParentResult, Async[Result]],
    parent: SubTask[ParentResult]
) extends SubTask[Result] {

  val dependencyType = ParentDependent

  protected def execute = parent.result.asAsync flatMap action
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

  protected def execute = compose(children).asAsync flatMap action
}

// TestSubTask

private[tasks] trait TestSubTask[Result] extends SubTask[Result]
