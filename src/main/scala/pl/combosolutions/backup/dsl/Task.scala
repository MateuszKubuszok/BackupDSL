package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scalaz._
import scalaz.OptionT._
import scalaz.std.scalaFuture._

abstract class Task[ParentResult,TaskResult] {

  private var tasks: mutable.MutableList[Task[TaskResult,_]] = collection.mutable.MutableList()

  def andThen[SubtaskResult](child: Task[TaskResult,SubtaskResult]): Task[TaskResult,SubtaskResult] = {
    tasks += child
    child
  }

  protected def backup(parentResult: ParentResult)(implicit settings: Settings): AsyncResult[TaskResult]

  protected def restore(parentResult: ParentResult)(implicit settings: Settings): AsyncResult[TaskResult]

  private[dsl] def performBackupWithResult(parentResult: ParentResult)(implicit settings: Settings): Unit = (for {
    result <- optionT[Future](backup(parentResult)(settings))
  } yield tasks.foreach(_.performBackupWithResult(result)(settings))).run

  private[dsl] def performRestoreWithResult(parentResult: ParentResult)(implicit settings: Settings): Unit = (for {
    result <- optionT[Future](restore(parentResult)(settings))
  } yield tasks.foreach(_.performBackupWithResult(result)(settings))).run
}

object Task extends Task[Unit,Unit] {

  def performBackup(implicit settings: Settings): Unit = performBackupWithResult(Unit)

  def performRestore(implicit settings: Settings): Unit = performBackupWithResult(Unit)

  override protected def backup(parentResult: Unit)(implicit settings: Settings) = Future successful Some(Unit)

  override protected def restore(parentResult: Unit)(implicit settings: Settings) = Future successful Some(Unit)
}
