package pl.combosolutions.backup.dsl.internals.elevation

import pl.combosolutions.backup.dsl.{Logging, ScriptConfig}
import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult
import pl.combosolutions.backup.dsl.internals.operations.Result
import pl.combosolutions.backup.dsl.internals.operations.{GenericProgram, JVMProgram}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.sys.process.Process

object ElevationFacade extends Logging {
  def createSockets(parallelElevations: Integer): List[ElevationIPCServerSocket] =
  (0 until parallelElevations) map (_ => new ElevationIPCServerSocket(ElevationIPC.ipcServer)) toList

  def createClients(sockets: List[ElevationIPCServerSocket]) =
  sockets map (new ElevationClient(_))

  def createServers(sockets: List[ElevationIPCServerSocket]): Process = {
    val program = JVMProgram(ElevatedExecutor.getClass, sockets map (_.port.toString))
    logger debug s"        preparing remote JVM executor with elevation"
    logger debug s"        ${program.toString}"
    program.run2Kill
  }
}

import ElevationFacade._

class ElevationFacade(config: ScriptConfig) extends Logging {

  private val parallelElevations = 2 // TODO: replace by config.parallelElevation when available

  logger trace s"        creating ${parallelElevations} elevation sockets"
  private val localSockets       = createSockets(parallelElevations)
  logger trace s"        created clients and servers"
  private val elevationClients   = createClients(localSockets)
  logger trace s"        created clients and servers"
  private val elevationServers   = createServers(localSockets)
  logger trace s"        created clients and servers"

  private val idleClients        = mutable.Queue() ++ elevationClients

  def runRemotely(program: GenericProgram): AsyncResult[Result[GenericProgram]] =
    borrowClient (_.executeRemote(program))

  def close = {
    localSockets foreach (_.close)
    elevationServers destroy
  }

  @tailrec
  private def reserveClient: ElevationClient = {
    synchronized {
      if   (idleClients.isEmpty) None
      else Some(idleClients.dequeue)
    } match {
      case Some(client) => client
      case None         => reserveClient
    }
  }

  private def freeClient(client: ElevationClient) = synchronized(idleClients enqueue client)

  private def borrowClient[T](task: ElevationClient => T): T = {
    val client = reserveClient

    val result = task(client)

    freeClient(client)

    result
  }
}
