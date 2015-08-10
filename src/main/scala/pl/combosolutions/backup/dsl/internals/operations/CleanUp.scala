package pl.combosolutions.backup.dsl.internals.operations

import scala.collection.mutable

trait Cleaner {
  type CleanUp = () => Unit

  private val tasks = mutable.Set[CleanUp]()

  private[internals] def addTask(cleanUp: CleanUp) = tasks += cleanUp

  protected def clean = tasks foreach (_())
}
