package pl.combosolutions.backup.dsl.internals

import scala.concurrent.Future
import scala.sys.process._

object SystemDependentOperations {
  lazy val current = new DebianOperations // TODO: implement for the rest of the world when needed
}
sealed abstract trait SystemDependentOperations {
  abstract def isSymbolicLink(fSPath: FSPath): Future[Option[Boolean]]

  abstract def obtainRepositories: List[Repository]
  abstract def storeRepositories(repositories: List[Repository]): Boolean

  type Packages <: Traversable[Package]
  abstract def isInstalled(_package: Package): Boolean
  abstract def areAllInstalled(packages: Packages): Boolean
  abstract def installAll(packages: Packages): Boolean
}

object LinuxCommonOperations {
  val fileIsSymlinkPattern = ".*: symbolic link to `.*'".r
}
abstract trait LinuxCommonOperations extends SystemDependentOperations with ProgramRunner {
  import LinuxCommonOperations._

  override def isSymbolicLink(fSPath: FSPath): Future[Option[Boolean]] = for {
    ro <- runProgram("file", fSPath toString)
    result <- ro map { case Result(_, output, _) => output exists (fileIsSymlinkPattern findFirstIn _ nonEmpty) }
  } yield result

}

object AptPackageOperations {
  val etcAptSourcesList = "/etc/apt/sources.list"
  val aptSourcesListComment = "#"
  val aptSourcePattern = "(deb|deb-src)[ \t]+(\\w+)([ \t]+\\w+)".r
}
abstract trait AptPackageOperations extends SystemDependentOperations {
  import AptPackageOperations._
  
  def parseLine(parsedLine: String) = for {
    line <- Some(parsedLine) if line.nonEmpty && !line.startsWith(aptSourcesListComment)
    lineMatch <- aptSourcePattern findFirstMatchIn line
  } yield AptRepository(lineMatch.group(1) == "deb-src",
                        lineMatch group 2,
                        lineMatch group 3 split("[ \t]+") toList)
  
  def parseInput(lines: Iterator[String]) = for {
    line <- lines
    repository <- parseLine(line)
  } yield repository

  override def obtainRepositories = parseInput(scala.io.Source.fromFile(etcAptSourcesList) getLines) toList
  override def storeRepositories(repositories: List[Repository]): Boolean

  override def installAll(packages: Packages): Boolean = ???

  override def areAllInstalled(packages: Packages): Boolean = ???

  override def isInstalled(_package: Package): Boolean = ???
}

class DebianOperations extends LinuxCommonOperations {
  override def obtainRepositories: List[Repository] = {
    // read lines from /etc/apt/sources.list
    // skip empty lines and lines starting with #
    // Match pattern for those that remains
    List()
  }
  override def storeRepositories(repositories: List[Repository]): Boolean

  override def installAll(packages: Packages): Boolean = ???

  override def areAllInstalled(packages: Packages): Boolean = ???

  override def isInstalled(_package: Package): Boolean = ???
}
