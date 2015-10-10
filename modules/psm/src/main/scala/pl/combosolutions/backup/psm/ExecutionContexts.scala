package pl.combosolutions.backup.psm

import java.util.concurrent.Executors

import pl.combosolutions.backup.Logging
import pl.combosolutions.backup.psm.DefaultsAndConstants.{ CommandThreadPoolSize, ProgramThreadPoolSize, TaskThreadPoolSize }

import scala.concurrent.ExecutionContext

object ExecutionContexts {

  private val commandProxy = new ExecutionContextsProxy
  private val programProxy = new ExecutionContextsProxy
  private val taskProxy = new ExecutionContextsProxy

  setCommandSize(CommandThreadPoolSize)
  setProgramSize(ProgramThreadPoolSize)
  setTaskSize(TaskThreadPoolSize)

  def setCommandSize(poolSize: Integer): Unit =
    commandProxy setExecutionContextTo (ExecutionContext fromExecutor (Executors newFixedThreadPool poolSize))

  def setProgramSize(poolSize: Integer): Unit =
    programProxy setExecutionContextTo (ExecutionContext fromExecutor (Executors newFixedThreadPool poolSize))

  def setTaskSize(poolSize: Integer): Unit =
    taskProxy setExecutionContextTo (ExecutionContext fromExecutor (Executors newFixedThreadPool poolSize))

  object Command {
    implicit val context: ExecutionContext = commandProxy
  }

  object Program {
    implicit val context: ExecutionContext = programProxy
  }

  object Task {
    implicit val context: ExecutionContext = taskProxy
  }

  private class ExecutionContextsProxy extends ExecutionContext with Logging {

    private var executionContext: Option[ExecutionContext] = None

    def setExecutionContextTo(context: ExecutionContext): Unit = executionContext = Some(context)

    override def execute(runnable: Runnable): Unit = executionContext foreach (_ execute runnable)

    override def reportFailure(cause: Throwable): Unit = executionContext foreach (_ reportFailure cause)
  }
}
