package pl.combosolutions.backup

import scala.collection.mutable

trait Cleaner {

  type CleanUp = () => Unit

  private val tasks = mutable.Set[CleanUp]()

  def addTask(cleanUp: CleanUp) = tasks += cleanUp

  protected def clean = tasks foreach (_())
}
