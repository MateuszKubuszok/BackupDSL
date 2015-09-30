package pl.combosolutions.backup.psm.elevation

import java.rmi.registry.LocateRegistry

import pl.combosolutions.backup.{ AsyncResult, Logging }
import pl.combosolutions.backup.psm.ExecutionContexts.Program.context
import pl.combosolutions.backup.psm.programs.{ GenericProgram, Result }

import scala.util.{ Failure, Success, Try }

private[elevation] class ElevationClient(var name: String, val remotePort: Integer) extends Logging {

  def executeRemote(program: GenericProgram): AsyncResult[Result[GenericProgram]] = AsyncResult {
    logger debug s"Sending remote command: ${program}"
    Try {
      server runRemote program
    } match {
      case Success(result) =>
        logger trace s"Received remote result ${program} for command ${program}"
        result
      case Failure(ex) =>
        logger error ("Remote execution failed", ex)
        throw ex
    }
  }

  def terminate: Unit = {
    logger debug "Terminate remote executor"
    Try(server terminate)
  }

  private lazy val serverInstance = (LocateRegistry getRegistry remotePort lookup name).asInstanceOf[ElevationServer]
  protected def server = serverInstance
}

