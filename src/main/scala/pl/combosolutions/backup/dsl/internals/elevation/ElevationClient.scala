package pl.combosolutions.backup.dsl.internals.elevation

import pl.combosolutions.backup.dsl.internals.operations.{GenericProgram, Result, Program}
import pl.combosolutions.backup.dsl.internals.operations.Program._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ElevationClient {
  def apply() = new ElevationClient(socket = new ElevationIPCSocket(ElevationIPC.ipcServer.accept))
}

class ElevationClient(socket: ElevationIPCSocket) {
  lazy val port = socket.port

  def executeRemote(program: GenericProgram): AsyncResult[Result[GenericProgram]] = Future {
    socket send program
    socket receiveResult
  }
}
