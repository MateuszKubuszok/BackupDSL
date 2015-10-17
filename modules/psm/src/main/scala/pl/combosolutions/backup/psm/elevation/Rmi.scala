package pl.combosolutions.backup.psm.elevation

import java.rmi.registry.{ LocateRegistry, Registry }
import java.rmi.server.UnicastRemoteObject

import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.PsmExceptionMessages.RemoteFailure
import pl.combosolutions.backup.psm.jvm.{ JVMProgram, JVMUtils }
import pl.combosolutions.backup.{ Logging, ReportException }

import scala.annotation.tailrec
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{ Await, Promise }
import scala.sys.process.Process
import scala.util.{ Failure, Random, Success, Try }

import RmiManager._

private[elevation] class RmiMutex {

  private val resultP = Promise[Unit]()
  private val resultF = resultP.future

  def waitForReadiness: Unit = synchronized {
    wait()
    Await.result(resultF, Inf)
  }

  def notifyReady: Unit = synchronized {
    resultP success Unit
    notifyAll()
  }

  def notifyFailure: Unit = synchronized {
    resultP tryComplete Try(ReportException onIllegalStateOf RemoteFailure)
    notifyAll()
  }
}

private[elevation] object RmiManager {

  val maxRegisterCreationAttempts = 10

  val possiblePorts = 1024 to 65536
}

private[elevation] class RmiManager(executorClass: Class[_ <: App]) extends Logging with ComponentsHelper {
  self: ElevationServiceComponent =>

  JVMUtils configureRMIFor executorClass

  def createClient(serverName: String, port: Integer): ElevationClient = new ElevationClient(serverName, port)

  def createServer(notifierName: String, serverName: String, port: Integer): Process = {
    val program = JVMProgram(executorClass, List(notifierName, serverName, port.toString))
    val elevated = DirectElevatorProgram(program, elevationService)
    logger debug s"Preparing elevated remote JVM executor (${executorClass.getSimpleName})"
    logger debug elevated
    elevated run2Kill
  }

  def createReadyNotifier(notifierName: String, registry: Registry, mutex: RmiMutex): ElevationReadyNotifier = {
    val notifier = ElevationReadyNotifier(() => mutex.notifyReady, () => mutex.notifyFailure)
    val stub = UnicastRemoteObject.exportObject(notifier, 0).asInstanceOf[ElevationReadyNotifier]
    registry bind (notifierName, stub)
    logger debug s"Preparing notifier informing client about server's readiness"
    stub
  }

  def createRegister: (Registry, Integer) = createRegisterOrFail()

  def findFreeName(registry: Registry): String = findFreeNameWithoutFailure(registry)

  @tailrec
  private def createRegisterOrFail(attemptsLeft: Integer = maxRegisterCreationAttempts): (Registry, Integer) = {
    val randomPort = possiblePorts(Random nextInt possiblePorts.size)

    // format: OFF
    Try (LocateRegistry createRegistry randomPort) match {
      case Success(registry) => (registry, randomPort)
      case Failure(ex)       => if (attemptsLeft <= 0) throw ex
                                else createRegisterOrFail(attemptsLeft - 1)
    }
    // format: ON
  }

  @tailrec
  private def findFreeNameWithoutFailure(registry: Registry): String = {
    val name = Random.nextLong().toString
    if (!registry.list.contains(name)) name
    else findFreeNameWithoutFailure(registry)
  }
}

private[elevation] trait RMIUserHelper {

  protected def configureRMI: Unit = JVMUtils configureRMIFor getClass

  protected def locateRegistryFor(remotePort: Integer): Registry = LocateRegistry getRegistry remotePort

  protected def exportServer(server: ElevationServer): ElevationServer =
    UnicastRemoteObject.exportObject(server, 0).asInstanceOf[ElevationServer]
}
