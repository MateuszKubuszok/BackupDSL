package pl.combosolutions.backup.psm.elevation

import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject

import pl.combosolutions.backup.Logging
import pl.combosolutions.backup.psm.jvm.JVMUtils

import scala.util.{ Failure, Success, Try }

trait ElevatedExecutorRMIHandler {

  protected def configureRMI = JVMUtils configureRMIFor getClass

  protected def locateRegistryFor(remotePort: Integer) = LocateRegistry getRegistry remotePort

  protected def exportServer(server: ElevationServer) =
    UnicastRemoteObject.exportObject(server, 0).asInstanceOf[ElevationServer]
}

class ElevatedExecutor(args: Array[String]) extends ElevatedExecutorRMIHandler with Logging {

  logger debug s"Starting remote ${getClass getSimpleName} with args: ${args toList}"

  val notifierName = args(0)
  val serverName = args(1)
  val remotePort = Integer valueOf args(2)

  Try {
    configureRMI
    val registry = locateRegistryFor(remotePort)
    val notifier = (registry lookup notifierName).asInstanceOf[ElevationReadyNotifier]

    Try {
      val server = ElevationServer()
      val stub = exportServer(server)
      registry.bind(serverName, stub)
    } match {
      case Success(_) => notifier.notifyReady
      case Failure(ex) =>
        notifier.notifyFailure
        throw ex
    }
  } match {
    case Success(_) => logger debug "Remote ready"
    case Failure(ex) =>
      logger error ("Remote failed", ex)
      terminateOnFailure
  }

  def terminateOnFailure = System exit -1
}

object ElevatedExecutor extends App {

  val executor = new ElevatedExecutor(args)
}
