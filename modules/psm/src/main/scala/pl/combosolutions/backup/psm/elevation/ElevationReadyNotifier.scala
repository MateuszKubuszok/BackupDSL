package pl.combosolutions.backup.psm.elevation

import java.rmi.{ Remote, RemoteException }

import pl.combosolutions.backup.Logging
import ElevationReadyNotifier.{ FailureListener, ReadyListener }

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
