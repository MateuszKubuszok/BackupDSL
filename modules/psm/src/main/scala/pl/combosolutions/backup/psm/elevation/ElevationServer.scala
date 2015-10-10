package pl.combosolutions.backup.psm.elevation

import java.rmi.{ Remote, RemoteException }

import pl.combosolutions.backup.{ Logging, Result }
import pl.combosolutions.backup.psm.programs.GenericProgram

import scala.concurrent.Await
import scala.concurrent.duration.Duration

sealed trait ElevationServer extends Remote {

  @throws(classOf[RemoteException])
  def runRemote(program: GenericProgram): Option[Result[GenericProgram]]

  @throws(classOf[RemoteException])
  def terminate: Unit
}

object ElevationServer {

  def apply(): ElevationServer = new ElevationServerImpl
}

private[elevation] final class ElevationServerImpl extends ElevationServer with Logging {

  def runRemote(program: GenericProgram): Option[Result[GenericProgram]] = {
    logger debug s"Run ${program} remotely"
    Await result (program.run, Duration.Inf)
  }

  def terminate: Unit = System exit 0
}
