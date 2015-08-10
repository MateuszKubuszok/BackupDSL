package pl.combosolutions.backup.dsl.internals.elevation

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult
import pl.combosolutions.backup.dsl.internals.operations.Result
import pl.combosolutions.backup.dsl.internals.operations.{GenericProgram, JVMProgram}

import scala.sys.process.Process
import scala.util.Random

object ElevationFacade extends Logging {

  private val executorClass = ElevatedExecutor.getClass

  def createClient(name: String, port: Integer) = new ElevationClient(name, port)

  def createServer(name: String, port: Integer): Process = {
    val program = JVMProgram(executorClass, List(name, port.toString))
    logger debug s"Preparing elevated remote JVM executor (${executorClass.getSimpleName})"
    logger debug program
    program.run2Kill
  }
}

import ElevationFacade._

class ElevationFacade extends Logging {

  val remoteName = Random.nextLong.toString
  val remotePort = 6802 // TODO: Change to something better

  val server = createServer(remoteName, remotePort)
  Thread sleep 1000
  val client = createClient(remoteName, remotePort)

  def runRemotely(program: GenericProgram): AsyncResult[Result[GenericProgram]] = client executeRemote program

  def close = client terminate
}
