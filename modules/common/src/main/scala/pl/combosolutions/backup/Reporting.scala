package pl.combosolutions.backup

import org.slf4j.LoggerFactory

trait Reporting {
  protected val reporter = Reporter
}

object Reporter {

  val impl = LoggerFactory getLogger "TaskReporter"

  def inform(obj: Object) = impl info s"${obj}"
  def more(obj: Object) = impl debug s"    ${obj}"
  def details(obj: Object) = impl trace s"        ${obj}"
  def error(obj: Object) = impl error s"        ${obj}"
  def error(obj: Object, throwable: Throwable) = impl error (s"        ${obj}", throwable)
}
