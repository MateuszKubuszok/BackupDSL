package pl.combosolutions.backup.dsl.internals.jvm

import java.io.File
import java.net.{URLClassLoader, URLDecoder}
import java.nio.file.{Files, Paths}

import pl.combosolutions.backup.dsl.Logging

import scala.collection.JavaConverters._
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

  lazy val rmiregistryExec = Seq(
    Paths.get(javaHome, "bin", "rmiregistry"),
    Paths.get(javaHome, "bin", "rmiregistry.exe")
  ) filter (Files exists _) head

  lazy val classPath = System getProperty "java.class.path"

  private lazy val jarClassPathPattern  = "jar:(file:)?([^!]+)!.+".r
  private lazy val fileClassPathPattern = "file:(.+).class".r

  def classPathFor[T](clazz: Class[T]): List[String] = {
    val pathToClass = getPathToClassFor(clazz)

    val propClassPath   = classPath.split(File.pathSeparator).toSet

    val loaderClassPath = clazz.getClassLoader.asInstanceOf[URLClassLoader].getURLs.map(_.toString).toSet

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

  def jriPathFor[T](clazz: Class[T]): String = {
    val pathToClass = getPathToClassFor(clazz)

    val propClassPath   = classPath.split(File.pathSeparator).map(file => s"file:${file}").toSet

    val loaderClassPath = clazz.getClassLoader.asInstanceOf[URLClassLoader].getURLs.map(_.toString).toSet

    val jarClassPath    = jarClassPathPattern.findFirstMatchIn(pathToClass) map { matcher =>
      val filePrefix = matcher group 1
      val jarDir     = Paths get (matcher group 2) getParent()
      s"jar:${filePrefix}${jarDir}"
    } toSet

    val fileClassPath   = fileClassPathPattern.findFirstMatchIn(pathToClass) map { matcher =>
      val suffix   = "/" + clazz.getName
      val fullPath = matcher group 1
      s"file:${fullPath substring (0, fullPath.length - suffix.length)}/"
    } toSet

    (propClassPath ++ loaderClassPath ++ jarClassPath ++ fileClassPath) reduce (_ + " " + _)
  }

  private def getPathToClassFor[T](clazz: Class[T]) = {
    val url = clazz getResource s"${clazz.getSimpleName}.class"
    Try { URLDecoder.decode(url.toString, "UTF-8") } match {
      case Success(classFilePath) => classFilePath
      case Failure(_)             => throw new IllegalStateException("") // TODO
    }
  }
}
