package pl.combosolutions.backup.psm.elevation

import pl.combosolutions.backup.{ AsyncResult, Logging }
import pl.combosolutions.backup.psm.operations.Cleaner
import pl.combosolutions.backup.psm.programs.{ GenericProgram, Result }

private[elevation] object ElevationFacade {

  def getFor(cleaner: Cleaner) = synchronized {
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
  private val mutex = new RmiMutex

  private val notifier = rmiManager createReadyNotifier (notifierName, registry, mutex)
  private val server = rmiManager createServer (notifierName, serverName, remotePort)
  mutex.waitForReadiness
  private val client = rmiManager createClient (serverName, remotePort)

  def runRemotely(program: GenericProgram): AsyncResult[Result[GenericProgram]] = client executeRemote program

  def close = {
    client.terminate
    server.destroy
  }
}
