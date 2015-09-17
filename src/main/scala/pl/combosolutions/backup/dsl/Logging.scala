package pl.combosolutions.backup.dsl

import org.slf4j.LoggerFactory
import ch.qos.logback.classic.LoggerContext

trait Logging {

  protected val logger = new LoggerWrapper(getClass)
}

class LoggerWrapper(clazz: Class[_]) {

  val impl = LoggerFactory getLogger clazz

  def info(obj: Object) = impl info s"${obj}"
  def debug(obj: Object) = impl debug s"    ${obj}"
  def trace(obj: Object) = impl trace s"        ${obj}"
  def warn(obj: Object) = impl warn s"    ${obj}"
  def error(obj: Object) = impl error s"${obj}"
  def error(obj: Object, ex: Throwable) = impl error (s"${obj}", ex)
}
