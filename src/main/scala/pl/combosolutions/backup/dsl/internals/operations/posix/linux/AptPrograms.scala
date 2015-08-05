package pl.combosolutions.backup.dsl.internals.operations.posix.linux

import pl.combosolutions.backup.dsl.internals.operations.posix.GrepFiles
import pl.combosolutions.backup.dsl.internals.operations.{Result, Program}
import pl.combosolutions.backup.dsl.internals.operations.posix.linux.AptOperations._
import pl.combosolutions.backup.dsl.internals.repositories.{AptRepository, VersionedPackage, Package}

object AptPrograms {
  type AptAddRepositoryInterpreter[U] = Result[AptAddRepository]#Interpreter[U]
  implicit val AptAddRepository2Boolean: AptAddRepositoryInterpreter[Boolean] = _.exitValue != 0

  type AptRemoveRepositoryInterpreter[U] = Result[AptRemoveRepository]#Interpreter[U]
  implicit val AptRemoveRepository2Boolean: AptRemoveRepositoryInterpreter[Boolean] = _.exitValue != 0

  type AptGetInstallInterpreter[U] = Result[AptGetInstall]#Interpreter[U]
  implicit val AptGetInstall2Boolean: AptGetInstallInterpreter[Boolean] = _.exitValue != 0

  type AptGetRemoveInterpreter[U] = Result[AptGetRemove]#Interpreter[U]
  implicit val AptGetRemove2Boolean: AptGetRemoveInterpreter[Boolean] = _.exitValue != 0

  type AptGetUpdateInterpreter[U] = Result[AptGetUpdate]#Interpreter[U]
  implicit val AptGetUpdate2Boolean: AptGetUpdateInterpreter[Boolean] = _.exitValue != 0

  type DpkgListInterpreter[U] = Result[DpkgList]#Interpreter[U]
  implicit val DpkgList2VersionedPackages: DpkgListInterpreter[List[VersionedPackage]] = result => for {
    line      <- result.stdout
    lineMatch <- installedPattern findFirstMatchIn line
  } yield VersionedPackage(lineMatch group 1,
                           lineMatch group 2)

  type ListAptReposInterpreter[U] = Result[ListAptRepos]#Interpreter[U]
  implicit val ListAptRepos2AptRepositories: ListAptReposInterpreter[List[AptRepository]] = result => for {
    line      <- result.stdout
    lineMatch <- aptSourcePattern findFirstMatchIn line
  } yield AptRepository(isSrc         = lineMatch group 1 equalsIgnoreCase "deb-src",
                        url           = lineMatch group 4,
                        branch        = lineMatch group 5,
                        areas         = lineMatch group 6 split "\\s+" toList,
                        architectures = lineMatch group 3 split "," toList)
}

case class AptAddRepository(repository: AptRepository) extends Program[AptAddRepository](
  "apt-add-repository",
  List("--yes", repository toString)
)

case class AptRemoveRepository(repository: AptRepository) extends Program[AptRemoveRepository](
  "apt-add-repository",
  List("--yes", "--remove", repository toString)
)

case class AptGetInstall(packages: List[Package]) extends Program[AptGetInstall](
  "apt-get",
  List("install", "-y", "-qq") ++ packages.map(_.name)
)

case class AptGetRemove(packages: List[Package]) extends Program[AptGetRemove](
  "apt-get",
  List("remove", "-y", "-qq") ++ packages.map(_.name)
)

trait AptGetUpdate extends Program[AptGetUpdate]
case object AptGetUpdate extends AptGetUpdate(
  "apt-get",
  List("update", "-y", "-qq")
)

trait DpkgList extends Program[DpkgList]
case object DpkgList extends DpkgList(
  "dpkg",
   List("--list")
)

trait ListAptRepos extends Program[ListAptRepos]
case object ListAptRepos extends GrepFiles(
  "^deb",
  List(etcAptSourcesMain, etcAptSourcesDir)
)

