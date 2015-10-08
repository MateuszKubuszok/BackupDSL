package pl.combosolutions.backup.psm.repositories

sealed abstract trait Repository

case class AptRepository(
    isSrc: Boolean,
    url: String,
    branch: String,
    areas: List[String],
    architectures: List[String]) extends Repository {

  override def toString = {
    val repoType = "deb" + (if (isSrc) "-src" else "")
    val areaList = if (areas.nonEmpty) areas reduce (_ + "," + _) else ""
    val architectureList = if (architectures.nonEmpty) s" [arch=${architectures reduce (_ + "," + _)}]" else ""
    s"$repoType$architectureList $url $branch $areaList"
  }
}
