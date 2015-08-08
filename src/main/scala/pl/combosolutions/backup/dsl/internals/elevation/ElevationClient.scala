package pl.combosolutions.backup.dsl.internals.elevation

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.operations.{GenericProgram, Result, Program}
import pl.combosolutions.backup.dsl.internals.operations.Program._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ElevationClient(serverSocket: ElevationIPCServerSocket) extends Logging {

  lazy val port = serverSocket.port

  lazy val socket = serverSocket.listen

  def executeRemote(program: GenericProgram): AsyncResult[Result[GenericProgram]] = Future {
    logger debug s"        executing remotely [${program}]"
    logger trace s"        sending onto remote [${program}]"
    socket send program
    logger trace s"        receiving remote [${program}]"
    socket receiveResult
  }
}
