package pl.combosolutions.backup

import org.slf4j.LoggerFactory

trait Logging {

  protected val logger = new LoggerWrapper(getClass)
}

class LoggerWrapper(clazz: Class[_]) {

  val impl = LoggerFactory getLogger clazz

  // format: OFF
  def info(obj: Object): Unit  = impl info  s"$obj"
  def debug(obj: Object): Unit = impl debug s"    $obj"
  def trace(obj: Object): Unit = impl trace s"        $obj"
  def warn(obj: Object): Unit  = impl warn  s"    $obj"
  def error(obj: Object): Unit = impl error s"$obj"
  def error(obj: Object, ex: Throwable): Unit = impl error (s"$obj", ex)
  // format: ON
}
