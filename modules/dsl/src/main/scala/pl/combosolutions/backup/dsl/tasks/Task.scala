package pl.combosolutions.backup.dsl.tasks

import pl.combosolutions.backup.{ AsyncResult, Logging }
import pl.combosolutions.backup.dsl.Settings
import pl.combosolutions.backup.psm.ExecutionContexts.Task.context

import scala.collection.mutable.MutableList
import scala.concurrent.Future
import scalaz.OptionT._
import scalaz.std.scalaFuture._

abstract class Task[PBR, PRR, BR, RR](description: String) extends Logging {

  private var tasks: MutableList[Task[BR, RR, _, _]] = MutableList()

  def andThen[SBR, SRR](child: Task[BR, RR, SBR, SRR]): Task[BR, RR, SBR, SRR] = {
    tasks += child
    child
  }

  protected def backup(parentResult: PBR)(implicit withSettings: Settings): AsyncResult[BR]

  protected def restore(parentResult: PRR)(implicit withSettings: Settings): AsyncResult[RR]

  private[dsl] def performBackupWithResult(parentResult: PBR)(implicit withSettings: Settings): Unit = {
    logger debug s"BACKUP  [${description}}] started"
    logger trace s"        with settings: ${withSettings}"
    (for {
      result <- optionT[Future](backup(parentResult)(withSettings))
    } yield {
      logger info s"BACKUP  [${description}}] succeeded"
      tasks.foreach(_.performBackupWithResult(result)(withSettings))
    }).run
  }

  private[dsl] def performRestoreWithResult(parentResult: PRR)(implicit withSettings: Settings): Unit = {
    logger debug s"RESTORE [${description}}] started"
    logger trace s"        with settings: ${withSettings}"
    (for {
      result <- optionT[Future](restore(parentResult)(withSettings))
    } yield {
      logger info s"RESTORE [${description}}] succeeded"
      tasks.foreach(_.performRestoreWithResult(result)(withSettings))
    }).run
  }
}
