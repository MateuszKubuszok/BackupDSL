package pl.combosolutions.backup

import scala.annotation.implicitNotFound
import scala.collection.mutable

@implicitNotFound("Required implicit Cleaner implementation - task dependency might need clean up procedure")
trait Cleaner {

  type CleanUp = () => Unit

  private val tasks = mutable.Set[CleanUp]()

  def addTask(cleanUp: CleanUp): Unit = tasks += cleanUp

  protected def clean(): Unit = tasks foreach (_())
}
