package pl.combosolutions.backup.dsl.internals.elevation

import java.rmi.{RemoteException, Remote}

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.elevation.ElevationReadyNotifier.Listener

trait ElevationReadyNotifier extends Remote {

  @throws(classOf[RemoteException])
  def notifyReady: Unit
}

object ElevationReadyNotifier {

  type Listener = () => Unit

  def apply(listener: Listener): ElevationReadyNotifier = new ElevationReadyNotifierImpl(listener)
}

class ElevationReadyNotifierImpl(listener: Listener) extends ElevationReadyNotifier with Logging {

  override def notifyReady: Unit = {
    logger trace "Notifies execution readiness"
    listener()
  }
}
