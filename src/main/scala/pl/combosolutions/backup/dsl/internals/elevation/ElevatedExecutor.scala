package pl.combosolutions.backup.dsl.internals.elevation

import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject

import pl.combosolutions.backup.dsl.Logging

import scala.util.{Failure, Success, Try}

object ElevatedExecutor extends App with Logging {

  logger debug s"Starting remote ${getClass getSimpleName} with args: ${args toList}"

  val remoteName = args(0)
  val remotePort = Integer valueOf args(1)

  Try {
    val server   = ElevationServer()
    val stub     = UnicastRemoteObject.exportObject(server, 0).asInstanceOf[ElevationServer]
    val registry = LocateRegistry createRegistry remotePort

    registry.bind(remoteName, stub)
  } match {
    case Success(_)  => logger debug "Remote ready"
    case Failure(ex) => logger error s"Remote failed: ${ex}"
                        System exit -1
  }
}
