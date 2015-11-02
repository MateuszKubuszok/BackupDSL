package pl.combosolutions.backup.tasks

import java.nio.file.{ Path, Paths }

import pl.combosolutions.backup.{ Async, ExecutionContexts, Reporting }
import ExecutionContexts.Task.context

import scala.collection.mutable

object SelectFiles extends Reporting {

  type Nothing2Result = () => Async[List[Path]]

  private def selectFilesAction(files: () => List[String]): Nothing2Result = () => Async {
    val paths = files() map (Paths get _)
    reporter inform s"Selected ${paths.length} files: $paths"
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
    parent: Configurator[PBR, _, List[Path], PRR, _, List[Path]]
) extends Configurator[List[Path], PBR, CBR, List[Path], PRR, CRR](Some(parent)) {

  val files: mutable.MutableList[String] = mutable.MutableList()

  override val builder = new SelectFiles[PBR, CBR, PRR, CRR](() => files.toList)
}
