package pl.combosolutions.backup

import org.slf4j.LoggerFactory

trait Reporting {

  protected val reporter = Reporter
}

object Reporter {

  val impl = LoggerFactory getLogger "TaskReporter"

  // format: OFF
  def inform(obj: Object): Unit  = impl info s"$obj"
  def more(obj: Object): Unit    = impl debug s"    $obj"
  def details(obj: Object): Unit = impl trace s"        $obj"
  def error(obj: Object): Unit   = impl error s"        $obj"
  def error(obj: Object, throwable: Throwable): Unit = impl error (s"        $obj", throwable)
  // format: ON
}
