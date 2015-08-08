package pl.combosolutions.backup.dsl

import org.slf4j.LoggerFactory
import ch.qos.logback.classic.LoggerContext

trait Logging {
  protected val logger = LoggerFactory getLogger getClass
}
