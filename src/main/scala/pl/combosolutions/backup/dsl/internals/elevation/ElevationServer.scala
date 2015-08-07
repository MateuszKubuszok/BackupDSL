package pl.combosolutions.backup.dsl.internals.elevation


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.util.Try

object ElevationServer {
  def apply(port: Integer) = new ElevationServer(socket = new ElevationIPCSocket(ElevationIPC.ipcClient(port)))
}

class ElevationServer(socket: ElevationIPCSocket) {
  def executeLocally: Unit = {
    socket.receiveProgram match {
      case Some(program) => Await.result(program.run, Duration.Inf) match {
          case Some(result) => socket send result
          case None         => socket error
        }

      case None => socket error
    }
  }

  def listen = Future {
    while (socket.isListening)
      Try (executeLocally)
    socket.close
  }
}
