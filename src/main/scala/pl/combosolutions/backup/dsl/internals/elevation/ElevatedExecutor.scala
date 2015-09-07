package pl.combosolutions.backup.dsl.internals.elevation

import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.jvm.JVMUtils

import scala.util.{Failure, Success, Try}

object ElevatedExecutor extends App with Logging {

  logger debug s"Starting remote ${getClass getSimpleName} with args: ${args toList}"

  val notifierName = args(0)
  val serverName   = args(1)
  val remotePort   = Integer valueOf args(2)

  Try {
    JVMUtils configureRMIFor getClass
    val registry = LocateRegistry getRegistry remotePort
    val notifier = (registry lookup notifierName).asInstanceOf[ElevationReadyNotifier]

    Try {
      val server   = ElevationServer()
      val stub     = UnicastRemoteObject.exportObject(server, 0).asInstanceOf[ElevationServer]
      registry.bind(serverName, stub)
    } match {
      case Success(_)  => notifier.notifyReady
      case Failure(ex) => notifier.notifyFailure
                          throw ex
    }
  } match {
    case Success(_)  => logger debug "Remote ready"
    case Failure(ex) => logger error("Remote failed", ex)
                        System exit -1
  }
}
