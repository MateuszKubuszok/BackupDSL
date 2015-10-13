package pl.combosolutions.backup.psm.programs.posix.linux

import pl.combosolutions.backup.Result
import pl.combosolutions.backup.psm.programs.Program
import pl.combosolutions.backup.psm.repositories.VersionedPackage
import pl.combosolutions.backup.psm.repositories.posix.linux.AptRepositoriesServiceComponent._

object DpkgPrograms {

  type DpkgListInterpreter[U] = Result[DpkgList]#Interpreter[U]
  implicit val DpkgList2VersionedPackages: DpkgListInterpreter[List[VersionedPackage]] = result => for {
    line <- result.stdout
    lineMatch <- installedPattern findFirstMatchIn line
  } yield VersionedPackage(lineMatch group 1, lineMatch group 2)
}

trait DpkgList extends Program[DpkgList]
case object DpkgList extends Program[DpkgList](
  "dpkg",
  List("--list")
)
