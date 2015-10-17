package pl.combosolutions.backup

import scala.concurrent.ExecutionContext

object TestExecutionContext {

  implicit val context = ExecutionContext.global
}
