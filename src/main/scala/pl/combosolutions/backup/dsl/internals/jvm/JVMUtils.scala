package pl.combosolutions.backup.dsl.internals.jvm

import java.io.File
import java.net.{URLClassLoader, URLDecoder}
import java.nio.file.{Files, Paths}

import pl.combosolutions.backup.dsl.Logging

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object JVMUtils extends Logging {
  lazy val javaHome = System getProperty "java.home"

  lazy val javaExe = Seq(
    Paths.get(javaHome, "bin", "java"),
    Paths.get(javaHome, "bin", "java.exe"),
    Paths.get(javaHome, "java"),
    Paths.get(javaHome, "java.exe")
  ) filter (Files exists _) head

  lazy val javaWExe = Seq(
    Paths.get(javaHome, "bin", "javaw"),
    Paths.get(javaHome, "bin", "javaw.exe"),
    Paths.get(javaHome, "javaw"),
    Paths.get(javaHome, "javaw.exe")
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
    } toList

    (propClassPath ++ loaderClassPath ++ jarClassPath ++ fileClassPath ++ Set(".")).toList
  }

  private def getPathToClassFor[T](clazz: Class[T]) = {
    val url = clazz getResource s"${clazz.getSimpleName}.class"
    Try { URLDecoder.decode(url.toString, "UTF-8") } match {
      case Success(classFilePath) => classFilePath
      case Failure(_)             => throw new IllegalStateException("") // TODO
    }
  }
}
