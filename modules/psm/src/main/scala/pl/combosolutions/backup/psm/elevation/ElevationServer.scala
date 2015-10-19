package pl.combosolutions.backup.psm.elevation

import java.rmi.{ Remote, RemoteException }

import pl.combosolutions.backup.{ Executable, Logging, Result }

import scala.concurrent.Await
import scala.concurrent.duration.Duration

sealed trait ElevationServer extends Remote {

  @throws(classOf[RemoteException])
  def runRemote(executable: Executable[_]): Option[Result[_]]

  @throws(classOf[RemoteException])
  def terminate(): Unit
}

object ElevationServer {

  def apply(): ElevationServer = new ElevationServerImpl
}

private[elevation] final class ElevationServerImpl extends ElevationServer with Logging {

  def runRemote(executable: Executable[_]): Option[Result[_]] = {
    logger debug s"Run $executable remotely"
    Await result (executable.run, Duration.Inf)
  }

  def terminate(): Unit = System exit 0
}
