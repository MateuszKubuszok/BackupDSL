package pl.combosolutions.backup.dsl.internals.jvm

import java.net.URLDecoder
import java.nio.file.{Path, Files, Paths}

import scala.util.{Failure, Success, Try}

object JVMUtils {
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

  private lazy val classPathPattern = "jar:(file:/)?([^!]+)!.+".r
  def classPathFor[T](clazz: Class[T]) = {
    val pathToClass = getPathToClassFor(clazz)
    classPathPattern.findFirstMatchIn(pathToClass.toString) match {
      case Some(matched) => matched group 2
      case None          => classPath
    }
  }

  private def getPathToClassFor[T](clazz: Class[T]) = {
    val url = clazz getResource s"${clazz.getSimpleName}.class"
    Try {
      val decoded = URLDecoder.decode(url.toString, "UTF-8")
      Paths get decoded toAbsolutePath
    } match {
      case Success(classFilePath) => classFilePath
      case Failure(_)             => throw new IllegalStateException("") // TODO
    }
  }
}
