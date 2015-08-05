package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult

import scala.collection.mutable
import scala.concurrent.Future
import scalaz._
import scalaz.OptionT._

abstract class Task[ParentResult, TaskResult] {
  private var tasks: mutable.MutableList[Task[TaskResult,_]] = collection.mutable.MutableList()

  def andThen(child: Task[TaskResult,_]): Unit = tasks += child

  protected def job(parentResult: ParentResult): AsyncResult[TaskResult]

  private[dsl] def executeWithResult(parentResult: ParentResult): Unit =
    for { result <- optionT[Future](job(parentResult)) } tasks.foreach(_.executeWithResult(result))
}

object Task extends Task[Unit, Unit] {
  def execute: Unit = executeWithResult(Unit)

  override protected def job(parentResult: Unit) = Future successful Some(Unit)
}
