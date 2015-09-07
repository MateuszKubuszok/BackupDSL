package pl.combosolutions.backup.dsl.internals.elevation

import java.rmi.{RemoteException, Remote}

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.programs.{GenericProgram, Result}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait ElevationServer extends Remote {

  @throws(classOf[RemoteException])
  def runRemote(program: GenericProgram): Option[Result[GenericProgram]]

  @throws(classOf[RemoteException])
  def terminate: Unit
}

object ElevationServer {
  def apply(): ElevationServer = new ElevationServerImpl
}

class ElevationServerImpl extends ElevationServer with Logging {

  def runRemote(program: GenericProgram): Option[Result[GenericProgram]] = {
    logger debug s"Run ${program} remotely"
    Await.result(program.run, Duration.Inf)
  }

  def terminate: Unit = System exit 0
}
