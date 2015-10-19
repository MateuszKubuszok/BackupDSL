package pl.combosolutions.backup

import scala.collection.mutable

trait Cleaner {

  type CleanUp = () => Unit

  private val tasks = mutable.Set[CleanUp]()

  def addTask(cleanUp: CleanUp): Unit = tasks += cleanUp

  protected def clean(): Unit = tasks foreach (_())
}
