package pl.combosolutions.backup.dsl.internals.elevation

import java.rmi.registry.{Registry, LocateRegistry}

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.operations.Program.AsyncResult
import pl.combosolutions.backup.dsl.internals.operations.{Cleaner, Result, GenericProgram, JVMProgram}

import scala.annotation.tailrec
import scala.sys.process.Process
import scala.util.{Failure, Success, Try, Random}

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

  private val (registry, remotePort) = getRegister()
  private val remoteName             = getName(registry)

  private val server = createServer(remoteName, remotePort)
  Thread sleep 1000 // TODO: find better solution
  private val client = createClient(remoteName, remotePort)

  def runRemotely(program: GenericProgram): AsyncResult[Result[GenericProgram]] = client executeRemote program

  def close = {
    client.terminate
    server.destroy
  }

  @tailrec
  private def getRegister(attemptsLeft: Integer = 10): (Registry, Integer) = {
    val possiblePorts = (1024 to 65536)
    val randomPort    = possiblePorts(scala.util.Random.nextInt(possiblePorts.size))

    Try (LocateRegistry createRegistry randomPort) match {
      case Success(registry) => (registry, randomPort)
      case Failure(ex)       => if (attemptsLeft <= 0) throw ex
                                else getRegister(attemptsLeft - 1)
    }
  }

  @tailrec
  private def getName(registry: Registry): String = {
    val name = Random.nextLong.toString
    if (!registry.list().contains(name)) name
    else getName(registry)
  }
}
