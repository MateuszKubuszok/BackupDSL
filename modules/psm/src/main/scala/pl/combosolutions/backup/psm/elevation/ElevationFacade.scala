package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup._

private[elevation] object ElevationFacade {

  def getFor(cleaner: Cleaner): ElevationFacade = synchronized {
    cleaner addTask elevationCleanUp
    elevationFacade
  }

  private val rmiManager = new RmiManager(ElevatedExecutor.getClass)
  private lazy val elevationFacade = new ElevationFacade(rmiManager)
  private lazy val elevationCleanUp = () => elevationFacade.close
}

private[elevation] class ElevationFacade(rmiManager: RmiManager) extends Logging {

  private val (registry, remotePort) = rmiManager.createRegister
  private val notifierName = rmiManager findFreeName registry
  private val serverName = rmiManager findFreeName registry
  private val mutex = createMutex

  private val notifier = rmiManager createReadyNotifier (notifierName, registry, mutex)
  private val server = rmiManager createServer (notifierName, serverName, remotePort)
  waitForReadiness
  private val client = rmiManager createClient (serverName, remotePort)

  def runRemotely[T <: Executable[T]](executable: Executable[T]): Async[Result[T]] = client executeRemote executable

  def close(): Unit = {
    client.terminate
    server.destroy()
  }

  protected def createMutex() = new RmiMutex

  protected def waitForReadiness() = mutex.waitForReadiness
}

private[elevation] trait ElevationFacadeComponent {

  def elevationFacadeFor(cleaner: Cleaner): ElevationFacade
}

private[elevation] trait ElevationFacadeComponentImpl extends ElevationFacadeComponent {

  def elevationFacadeFor(cleaner: Cleaner): ElevationFacade = ElevationFacade getFor cleaner
}
