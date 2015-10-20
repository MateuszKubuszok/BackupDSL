package pl.combosolutions.backup.psm.elevation

import java.rmi.registry.LocateRegistry

import pl.combosolutions.backup._
import ExecutionContexts.Program.context

import scala.util.{ Failure, Success, Try }

private[elevation] class ElevationClient(var name: String, val remotePort: Integer) extends Logging {

  def executeRemote[T <: Executable[T]](executable: Executable[T]): Async[Result[T]] = Async {
    logger debug s"Sending remote command: $executable"
    Try {
      server runRemote executable
    } match {
      case Success(result) =>
        logger trace s"Received remote result for command $executable"
        result.asInstanceOf[Option[Result[T]]]
      case Failure(ex) =>
        logger error ("Remote execution failed", ex)
        throw ex
    }
  }

  def terminate(): Unit = {
    logger debug "Terminate remote executor"
    Try(server terminate)
  }

  // $COVERAGE-OFF$ Impossible to test without PowerMock
  private lazy val serverInstance = (LocateRegistry getRegistry remotePort lookup name).asInstanceOf[ElevationServer]
  protected def server = serverInstance
  // $COVERAGE-ON$
}

