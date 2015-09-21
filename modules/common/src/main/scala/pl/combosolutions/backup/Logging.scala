package pl.combosolutions.backup

import org.slf4j.LoggerFactory

trait Logging {

  protected val logger = new LoggerWrapper(getClass)
}

class LoggerWrapper(clazz: Class[_]) {

  val impl = LoggerFactory getLogger clazz

  // format: OFF
  def info(obj: Object)  = impl info  s"${obj}"
  def debug(obj: Object) = impl debug s"    ${obj}"
  def trace(obj: Object) = impl trace s"        ${obj}"
  def warn(obj: Object)  = impl warn  s"    ${obj}"
  def error(obj: Object) = impl error s"${obj}"
  def error(obj: Object, ex: Throwable) = impl error (s"${obj}", ex)
  // format: ON
}
