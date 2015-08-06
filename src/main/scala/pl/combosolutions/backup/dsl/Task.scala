package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult

import scala.collection.mutable.MutableList
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scalaz._
import scalaz.OptionT._
import scalaz.std.scalaFuture._

abstract class Task[PBR,PRR,BR,RR] {

  private var tasks: MutableList[Task[BR,RR,_,_]] = MutableList()

  def andThen[SBR,SRR](child: Task[BR,RR,SBR,SRR]): Task[BR,RR,SBR,SRR] = {
    tasks += child
    child
  }

  protected def backup(parentResult: PBR)(implicit withSettings: Settings): AsyncResult[BR]

  protected def restore(parentResult: PRR)(implicit withSettings: Settings): AsyncResult[RR]

  private[dsl] def performBackupWithResult(parentResult: PBR)(implicit withSettings: Settings): Unit = (for {
    result <- optionT[Future](backup(parentResult)(withSettings))
  } yield tasks.foreach(_.performBackupWithResult(result)(withSettings))).run

  private[dsl] def performRestoreWithResult(parentResult: PRR)(implicit withSettings: Settings): Unit = (for {
    result <- optionT[Future](restore(parentResult)(withSettings))
  } yield tasks.foreach(_.performRestoreWithResult(result)(withSettings))).run
}
