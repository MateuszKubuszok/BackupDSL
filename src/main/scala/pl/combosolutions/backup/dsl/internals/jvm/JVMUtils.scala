package pl.combosolutions.backup.dsl.internals.jvm

import java.io.File
import java.lang.management.ManagementFactory
import java.net.{URLClassLoader, URLDecoder}
import java.nio.file.{Files, Paths}

import pl.combosolutions.backup.dsl.Logging

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

object JVMUtils extends Logging {
  lazy val javaHome = System getProperty "java.home"

  lazy val javaExec = Seq(
    Paths.get(javaHome, "bin", "java"),
    Paths.get(javaHome, "bin", "java.exe")
  ) filter (Files exists _) head

  lazy val javaWExec = Seq(
    Paths.get(javaHome, "bin", "javaw"),
    Paths.get(javaHome, "bin", "javaw.exe")
  ) filter (Files exists _) head

  lazy val classPath = System getProperty "java.class.path"

  private lazy val jarClassPathPattern  = "jar:(file:)?([^!]+)!.+".r
  private lazy val fileClassPathPattern = "file:(.+).class".r

  def classPathFor[T](clazz: Class[T]): List[String] = {
    val pathToClass = getPathToClassFor(clazz)

    val propClassPath   = classPath split File.pathSeparator toSet

    val loaderClassPath = clazz.getClassLoader.asInstanceOf[URLClassLoader].getURLs.map(_.getFile).toSet

    val jarClassPath    = jarClassPathPattern.findFirstMatchIn(pathToClass) map { matcher =>
      val jarDir = Paths get (matcher group 2) getParent()
      s"${jarDir}/*"
    } toSet

    val fileClassPath   = fileClassPathPattern.findFirstMatchIn(pathToClass) map { matcher =>
      val suffix   = "/" + clazz.getName
      val fullPath = matcher group 1
      fullPath substring (0, fullPath.length - suffix.length)
    } toSet

    (propClassPath ++ loaderClassPath ++ jarClassPath ++ fileClassPath ++ Set(".")).toList
  }

  def configureRMIFor[T](clazz: Class[T]): Unit = {
    val classPath = classPathFor(clazz)
    val codebase  = if (classPath isEmpty) ""
                    else classPath map (new File(_).getAbsoluteFile.toURI.toURL.toString) reduce (_ + " " + _)

    logger trace s"Set java.rmi.server.codebase to: $codebase"
    System setProperty ("java.rmi.server.codebase", codebase)

    logger trace s"Set java.rmi.server.disableHttp to: true"
    System setProperty ("java.rmi.server.disableHttp", "true")
  }

  def jvmArgs: List[String] = ManagementFactory.getRuntimeMXBean.getInputArguments.toList

  def jvmArgsExceptDebug: List[String] = jvmArgs filterNot isDebugArg

  def jvmDebugArgs: List[String] = jvmArgs filter isDebugArg

  private def getPathToClassFor[T](clazz: Class[T]) = {
    val url = clazz getResource s"${clazz.getSimpleName}.class"
    Try (URLDecoder decode (url.toString, "UTF-8")) match {
      case Success(classFilePath) => classFilePath
      case Failure(_)             => throw new IllegalStateException("") // TODO
    }
  }

  private def isDebugArg(arg: String) = {
    val lcArg = arg.toLowerCase
    (lcArg == "-xdebug") || (lcArg == "-xrunjdwp") || (lcArg startsWith "-agentlib:jdwp")
  }
}
