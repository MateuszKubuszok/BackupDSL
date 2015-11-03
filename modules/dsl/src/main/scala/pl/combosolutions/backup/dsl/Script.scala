package pl.combosolutions.backup.dsl

import java.lang.System.exit

import pl.combosolutions.backup.{ Cleaner, Logging }

import scala.util.{ Failure, Success, Try }

abstract class Script(override val name: String) extends ArgumentParser with ScriptExecutor with Cleaner with Logging {

  final def printHelp(): Unit = parser.showTryHelp

  private[dsl] final def runAndWait(args: Array[String]): Try[Unit] =
    Try (parser parse (args, ScriptConfig()) foreach execute) recoverWith {
      case ex: Throwable =>
        logger error ("error during execution", ex)
        Failure(ex)
    }

  final def main(args: Array[String]): Unit = {
    val exitCode = runAndWait(args) match {
      case Success(_) => 0
      case Failure(_) => 1
    }
    clean
    exit(exitCode) // apparently all those uninitialized lazy values would wait indefinitely, we have to help it finish
  }
}
