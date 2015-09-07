package pl.combosolutions.backup.dsl.internals.elevation

import java.rmi.registry.{Registry, LocateRegistry}
import java.rmi.server.UnicastRemoteObject

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.DefaultsAndConsts
import pl.combosolutions.backup.dsl.internals.jvm.{JVMUtils, JVMProgram}
import pl.combosolutions.backup.dsl.internals.programs.{GenericProgram, Program, Result}
import Program.AsyncResult
import pl.combosolutions.backup.dsl.internals.operations.Cleaner

import scala.annotation.tailrec
import scala.sys.process.Process
import scala.util.{Failure, Success, Try, Random}

object ElevationFacade extends Logging {

  def getFor(cleaner: Cleaner) = synchronized {
    cleaner addTask elevationCleanUp
    elevationFacade
  }

  private      val executorClass    = ElevatedExecutor.getClass
  private lazy val elevationFacade  = new ElevationFacade
  private lazy val elevationCleanUp = () => elevationFacade.close

  private def createClient(serverName: String, port: Integer) = new ElevationClient(serverName, port)

  private def createServer(notifierName: String, serverName: String, port: Integer): Process = {
    val program  = JVMProgram(executorClass, List(notifierName, serverName, port.toString))
    val elevated = DirectElevatorProgram(program)
    logger debug s"Preparing elevated remote JVM executor (${executorClass.getSimpleName})"
    logger debug elevated
    elevated.run2Kill
  }

  private def createReadyNotifier(notifierName: String, registry: Registry, mutex: Mutex) = {
    val notifier = ElevationReadyNotifier(() => mutex.notifyReady, () => mutex.notifyFailure)
    val stub     = UnicastRemoteObject.exportObject(notifier, 0).asInstanceOf[ElevationReadyNotifier]
    registry bind(notifierName, stub)
    logger debug s"Preparing notifier informing client about server's readiness"
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
    if (!registry.list.contains(name)) name
    else getName(registry)
  }

  private[elevation] class Mutex {

    private var failureOccurred = false

    def waitForReadiness = synchronized {
      wait
      if (failureOccurred)
        throw new IllegalStateException(DefaultsAndConsts.exceptionRemoteFailed)
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

  private val (registry, remotePort) = getRegister()
  private val notifierName           = getName(registry)
  private val serverName             = getName(registry)
  private val mutex                  = new Mutex

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
