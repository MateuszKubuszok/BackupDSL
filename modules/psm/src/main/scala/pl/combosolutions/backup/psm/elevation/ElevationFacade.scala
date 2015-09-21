package pl.combosolutions.backup.psm.elevation

import java.rmi.registry.{ LocateRegistry, Registry }
import java.rmi.server.UnicastRemoteObject

import pl.combosolutions.backup.{ AsyncResult, ReportException, Logging }
import pl.combosolutions.backup.psm.PsmExceptionMessages
import PsmExceptionMessages.RemoteFailure
import pl.combosolutions.backup.psm.jvm.{JVMProgram, JVMUtils}
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.{Result, GenericProgram}

import scala.annotation.tailrec
import scala.sys.process.Process
import scala.util.{ Failure, Success, Try, Random }

object ElevationFacade extends Logging {

  def getFor(cleaner: Cleaner) = synchronized {
    cleaner addTask elevationCleanUp
    elevationFacade
  }

  private val executorClass = ElevatedExecutor.getClass
  private lazy val elevationFacade = new ElevationFacade
  private lazy val elevationCleanUp = () => elevationFacade.close

  private def createClient(serverName: String, port: Integer) = new ElevationClient(serverName, port)

  private def createServer(notifierName: String, serverName: String, port: Integer): Process = {
    val program = JVMProgram(executorClass, List(notifierName, serverName, port.toString))
    val elevated = DirectElevatorProgram(program)
    logger debug s"Preparing elevated remote JVM executor (${executorClass.getSimpleName})"
    logger debug elevated
    elevated run2Kill
  }

  private def createReadyNotifier(notifierName: String, registry: Registry, mutex: Mutex) = {
    val notifier = ElevationReadyNotifier(() => mutex.notifyReady, () => mutex.notifyFailure)
    val stub = UnicastRemoteObject.exportObject(notifier, 0).asInstanceOf[ElevationReadyNotifier]
    registry bind (notifierName, stub)
    logger debug s"Preparing notifier informing client about server's readiness"
  }

  @tailrec
  private def createRegister(attemptsLeft: Integer = 10): (Registry, Integer) = {
    val possiblePorts = (1024 to 65536)
    val randomPort = possiblePorts(Random nextInt possiblePorts.size)

    // format: OFF
    Try (LocateRegistry createRegistry randomPort) match {
      case Success(registry) => (registry, randomPort)
      case Failure(ex)       => if (attemptsLeft <= 0) throw ex
                                else createRegister(attemptsLeft - 1)
    }
    // format: ON
  }

  @tailrec
  private def findFreeName(registry: Registry): String = {
    val name = Random.nextLong.toString
    if (!registry.list.contains(name)) name
    else findFreeName(registry)
  }

  private[elevation] class Mutex {

    private var failureOccurred = false

    def waitForReadiness = synchronized {
      wait
      if (failureOccurred)
        ReportException onIllegalStateOf RemoteFailure
    }

    def notifyReady = synchronized(notifyAll)

    def notifyFailure = synchronized {
      failureOccurred = true
      notifyAll
    }
  }
}

import ElevationFacade._

class ElevationFacade private () extends Logging {

  JVMUtils configureRMIFor executorClass

  private val (registry, remotePort) = createRegister()
  private val notifierName = findFreeName(registry)
  private val serverName = findFreeName(registry)
  private val mutex = new Mutex

  createReadyNotifier(notifierName, registry, mutex)
  private val server = createServer(notifierName, serverName, remotePort)
  mutex.waitForReadiness
  private val client = createClient(serverName, remotePort)

  def runRemotely(program: GenericProgram): AsyncResult[Result[GenericProgram]] = client executeRemote program

  def close = {
    client.terminate
    server.destroy
  }
}
