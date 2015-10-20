package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.Logging

import scala.util.{ Failure, Success, Try }

class ElevatedExecutor(args: Array[String]) extends RMIUserHelper with Logging {

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

  // $COVERAGE-OFF$ Impossible to test without PowerMock
  def terminateOnFailure(): Unit = System exit -1
  // $COVERAGE-ON$
}

object ElevatedExecutor extends App {

  val executor = new ElevatedExecutor(args)
}
