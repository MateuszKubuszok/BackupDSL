package pl.combosolutions.backup.dsl.internals.elevation

import java.rmi.{RemoteException, Remote}

import pl.combosolutions.backup.dsl.Logging
import pl.combosolutions.backup.dsl.internals.elevation.ElevationReadyNotifier.{FailureListener, ReadyListener}

trait ElevationReadyNotifier extends Remote {

  @throws(classOf[RemoteException])
  def notifyFailure: Unit

  @throws(classOf[RemoteException])
  def notifyReady: Unit
}

object ElevationReadyNotifier {

  type FailureListener = () => Unit

  type ReadyListener = () => Unit

  def apply(readyListener: ReadyListener, failureListener: FailureListener): ElevationReadyNotifier =
    new ElevationReadyNotifierImpl(readyListener, failureListener)
}

class ElevationReadyNotifierImpl(readyListener: ReadyListener, failureListener: FailureListener)
    extends ElevationReadyNotifier with Logging {

  override def notifyFailure: Unit = {
    logger error "Notifies execution failure"
    failureListener()
  }

  override def notifyReady: Unit = {
    logger trace "Notifies execution readiness"
    readyListener()
  }
}
