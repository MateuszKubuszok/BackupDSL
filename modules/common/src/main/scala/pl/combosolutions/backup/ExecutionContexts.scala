package pl.combosolutions.backup

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

import DefaultsAndConstants._

private[backup] trait ExecutionContexts {

  private[backup] val commandProxy = new ExecutionContextsProxy
  private[backup] val programProxy = new ExecutionContextsProxy
  private[backup] val taskProxy = new ExecutionContextsProxy

  def setCommandSize(poolSize: Integer): Unit =
    commandProxy setExecutionContextTo (ExecutionContext fromExecutor (Executors newFixedThreadPool poolSize))

  def setProgramSize(poolSize: Integer): Unit =
    programProxy setExecutionContextTo (ExecutionContext fromExecutor (Executors newFixedThreadPool poolSize))

  def setTaskSize(poolSize: Integer): Unit =
    taskProxy setExecutionContextTo (ExecutionContext fromExecutor (Executors newFixedThreadPool poolSize))

  protected class ExecutionContextsProxy extends ExecutionContext with Logging {

    private[backup] var executionContext: Option[ExecutionContext] = None

    def setExecutionContextTo(context: ExecutionContext): Unit = executionContext = Some(context)

    override def execute(runnable: Runnable): Unit = executionContext foreach (_ execute runnable)

    override def reportFailure(cause: Throwable): Unit = executionContext foreach (_ reportFailure cause)
  }
}

private[backup] trait DefaultECVPoolSizes {
  self: ExecutionContexts =>

  val defaultCommandThreadPoolSize = CommandThreadPoolSize
  val defaultProgramThreadPoolSize = ProgramThreadPoolSize
  val defaultTaskThreadPoolSize = TaskThreadPoolSize

  setCommandSize(defaultCommandThreadPoolSize)
  setProgramSize(defaultProgramThreadPoolSize)
  setTaskSize(defaultTaskThreadPoolSize)
}

object ExecutionContexts extends ExecutionContexts with DefaultECVPoolSizes {

  object Command {
    implicit val context: ExecutionContext = commandProxy
  }

  object Program {
    implicit val context: ExecutionContext = programProxy
  }

  object Task {
    implicit val context: ExecutionContext = taskProxy
  }
}
