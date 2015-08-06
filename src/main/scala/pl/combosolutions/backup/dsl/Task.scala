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

  protected def backup(parentResult: PBR)(implicit settings: Settings): AsyncResult[BR]

  protected def restore(parentResult: PRR)(implicit settings: Settings): AsyncResult[RR]

  private[dsl] def performBackupWithResult(parentResult: PBR)(implicit settings: Settings): Unit = (for {
    result <- optionT[Future](backup(parentResult)(settings))
  } yield tasks.foreach(_.performBackupWithResult(result)(settings))).run

  private[dsl] def performRestoreWithResult(parentResult: PRR)(implicit settings: Settings): Unit = (for {
    result <- optionT[Future](restore(parentResult)(settings))
  } yield tasks.foreach(_.performRestoreWithResult(result)(settings))).run
}
