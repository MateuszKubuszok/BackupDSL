package pl.combosolutions.backup.dsl.internals.elevation

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult
import pl.combosolutions.backup.dsl.internals.operations.{Cleaner, Result, GenericProgram, JVMProgram}

import scala.sys.process.Process
import scala.util.Random

object ElevationFacade extends Logging {

  private val executorClass = ElevatedExecutor.getClass
  private lazy val elevationFacade  = new ElevationFacade
  private lazy val elevationCleanUp = () => elevationFacade.close

  def createClient(name: String, port: Integer) = new ElevationClient(name, port)

  def createServer(name: String, port: Integer): Process = {
    val program = JVMProgram(executorClass, List(name, port.toString))
    logger debug s"Preparing elevated remote JVM executor (${executorClass.getSimpleName})"
    logger debug program
    program.run2Kill
  }

  def getFor(cleaner: Cleaner) = synchronized {
    cleaner addTask elevationCleanUp
    elevationFacade
  }
}

import ElevationFacade._

class ElevationFacade private () extends Logging {

  private val remoteName = Random.nextLong.toString
  private val remotePort = 6802 // TODO: Change to something better

  private val server = createServer(remoteName, remotePort)
  Thread sleep 1000 // TODO: find better solution
  private val client = createClient(remoteName, remotePort)

  def runRemotely(program: GenericProgram): AsyncResult[Result[GenericProgram]] = client executeRemote program

  def close = client terminate
}
