package pl.combosolutions.backup.dsl.internals.elevation

import java.rmi.registry.LocateRegistry

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.operations.{GenericProgram, Result}
import pl.combosolutions.backup.dsl.internals.operations.Program._

import scala.concurrent.Future
import scala.util.{Failure, Try, Success}

class ElevationClient(var name: String, val port: Integer) extends Logging {

  val registry = LocateRegistry getRegistry("127.0.0.1", port)
  val stub     = (registry lookup name).asInstanceOf[ElevationServer]

  def executeRemote(program: GenericProgram): AsyncResult[Result[GenericProgram]] = Try {
    logger debug s"Sending remote command: ${program}"
    stub runRemote program
  } match {
    case Success(result) => logger trace s"Received remote result ${program} for command ${program}"
                            Future successful result
    case Failure(ex)     => logger error s"Remote execution failed: ${ex}"
                            Future failed ex
  }

  def terminate: Unit = {
    logger debug "Terminate remote executor"
    Try (stub terminate)
  }
}

