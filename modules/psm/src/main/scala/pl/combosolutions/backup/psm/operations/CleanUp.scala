package pl.combosolutions.backup.psm.operations

import scala.collection.mutable

trait Cleaner {

  type CleanUp = () => Unit

  private val tasks = mutable.Set[CleanUp]()

  private[psm] def addTask(cleanUp: CleanUp) = tasks += cleanUp

  protected def clean = tasks foreach (_())
}
