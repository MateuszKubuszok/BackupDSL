package pl.combosolutions.backup.psm.elevation

import java.rmi.registry.{ LocateRegistry, Registry }
import java.rmi.server.UnicastRemoteObject

import pl.combosolutions.backup.psm.ComponentsHelper
import pl.combosolutions.backup.psm.PsmExceptionMessages.RemoteFailure
import pl.combosolutions.backup.psm.jvm.{ JVMProgram, JVMUtils }
import pl.combosolutions.backup.{ Logging, ReportException }

import scala.annotation.tailrec
import scala.sys.process.Process
import scala.util.{ Failure, Random, Success, Try }

private[elevation] class RmiMutex {

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

private[elevation] class RmiManager(executorClass: Class[_ <: App]) extends Logging with ComponentsHelper {
  self: ElevationServiceComponent =>

  JVMUtils configureRMIFor executorClass

  def createClient(serverName: String, port: Integer) = new ElevationClient(serverName, port)

  def createServer(notifierName: String, serverName: String, port: Integer): Process = {
    val program = JVMProgram(executorClass, List(notifierName, serverName, port.toString))
    val elevated = DirectElevatorProgram(program, elevationService)
    logger debug s"Preparing elevated remote JVM executor (${executorClass.getSimpleName})"
    logger debug elevated
    elevated run2Kill
  }

  def createReadyNotifier(notifierName: String, registry: Registry, mutex: RmiMutex) = {
    val notifier = ElevationReadyNotifier(() => mutex.notifyReady, () => mutex.notifyFailure)
    val stub = UnicastRemoteObject.exportObject(notifier, 0).asInstanceOf[ElevationReadyNotifier]
    registry bind (notifierName, stub)
    logger debug s"Preparing notifier informing client about server's readiness"
    stub
  }

  def createRegister() = _createRegister()

  def findFreeName(registry: Registry) = _findFreeName(registry)

  @tailrec
  private def _createRegister(attemptsLeft: Integer = 10): (Registry, Integer) = {
    val possiblePorts = (1024 to 65536)
    val randomPort = possiblePorts(Random nextInt possiblePorts.size)

    // format: OFF
    Try (LocateRegistry createRegistry randomPort) match {
      case Success(registry) => (registry, randomPort)
      case Failure(ex)       => if (attemptsLeft <= 0) throw ex
                                else _createRegister(attemptsLeft - 1)
    }
    // format: ON
  }

  @tailrec
  private def _findFreeName(registry: Registry): String = {
    val name = Random.nextLong.toString
    if (!registry.list.contains(name)) name
    else _findFreeName(registry)
  }
}
