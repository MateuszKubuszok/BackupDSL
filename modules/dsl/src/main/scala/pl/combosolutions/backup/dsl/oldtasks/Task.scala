package pl.combosolutions.backup.dsl.oldtasks

import pl.combosolutions.backup.tasks.Settings
import pl.combosolutions.backup.{ Async, Logging }
import pl.combosolutions.backup.psm.ExecutionContexts.Task.context

import scala.collection.mutable
import scala.concurrent.Future
import scalaz.OptionT._
import scalaz.std.scalaFuture._

abstract class Task[PBR, PRR, BR, RR](description: String) extends Logging {

  private var tasks: mutable.MutableList[Task[BR, RR, _, _]] = mutable.MutableList()

  def andThen[SBR, SRR](child: Task[BR, RR, SBR, SRR]): Task[BR, RR, SBR, SRR] = {
    tasks += child
    child
  }

  protected def backup(parentResult: PBR)(implicit withSettings: Settings): Async[BR]

  protected def restore(parentResult: PRR)(implicit withSettings: Settings): Async[RR]

  private[oldtasks] def performBackupWithResult(parentResult: PBR)(implicit withSettings: Settings): Unit = {
    logger debug s"BACKUP  [$description] started"
    logger trace s"        with settings: $withSettings"
    (for {
      result <- optionT[Future](backup(parentResult))
    } yield {
      logger info s"BACKUP  [$description] succeeded"
      tasks.foreach(_.performBackupWithResult(result))
    }).run
  }

  private[oldtasks] def performRestoreWithResult(parentResult: PRR)(implicit withSettings: Settings): Unit = {
    logger debug s"RESTORE [$description] started"
    logger trace s"        with settings: $withSettings"
    (for {
      result <- optionT[Future](restore(parentResult))
    } yield {
      logger info s"RESTORE [$description] succeeded"
      tasks.foreach(_.performRestoreWithResult(result))
    }).run
  }
}
