package pl.combosolutions.backup.tasks

import java.nio.file.{ Path, Paths }

import pl.combosolutions.backup.{ Async, Reporting }
import pl.combosolutions.backup.psm.ExecutionContexts.Task.context

import scala.collection.mutable

object SelectFiles extends Reporting {

  private def selectFilesAction(files: () => List[String]): () => Async[List[Path]] = () => Async {
    val paths = files() map (Paths get _)
    // TODO check if files exists
    reporter inform s"Selected ${paths.length} files for copying: $paths"
    Some(paths)
  }

  class SelectFilesAction[ParentResult, ChildResult](files: () => List[String])
    extends IndependentSubTaskBuilder[List[Path], ParentResult, ChildResult](selectFilesAction(files))
}

import SelectFiles._

class SelectFiles[PBR, CBR, PRR, CRR](files: () => List[String])
  extends TaskBuilder[List[Path], PBR, CBR, List[Path], PRR, CRR](
    new SelectFilesAction[PBR, CBR](files),
    new SelectFilesAction[PRR, CRR](files)
  )

class SelectFilesConfigurator[PBR, CBR, PRR, CRR](
    parent:                       MutableConfigurator[PBR, _, List[Path], PRR, _, List[Path]],
    override val initialSettings: Settings
) extends MutableConfigurator[List[Path], PBR, CBR, List[Path], PRR, CRR](Some(parent), initialSettings) {
  self: MutableConfigurator[List[Path], PBR, CBR, List[Path], PRR, CRR] =>

  implicit val withSettings: Settings = settingsProxy

  val files: mutable.MutableList[String] = mutable.MutableList()

  override def taskBuilder = new SelectFiles(() => files.toList)
}
