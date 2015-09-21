package pl.combosolutions.backup.dsl.tasks.beta

import java.nio.file.{ Path, Paths }

import pl.combosolutions.backup.{ AsyncResult, Reporting }
import pl.combosolutions.backup.dsl.tasks.beta.SelectFiles.SelectFilesAction
import pl.combosolutions.backup.psm.ExecutionContexts.Task.context

object SelectFiles extends Reporting {

  private def selectFilesAction(files: List[String]): () => AsyncResult[List[Path]] = () => AsyncResult { () =>
    val paths = files map (Paths get _)
    reporter inform s"Selected ${paths.length} files for copying: $paths"
    paths
  }

  class SelectFilesAction[ParentResult, ChildResult](files: List[String])
    extends IndependentSubTaskBuilder[List[Path], ParentResult, ChildResult](selectFilesAction(files))
}

class SelectFiles[ParentBackupResult, ChildBackupResult, ParentRestoreResult, ChildRestoreResult](files: List[String])
  extends TaskBuilder[List[Path], ParentBackupResult, ChildBackupResult, List[Path], ParentRestoreResult, ChildRestoreResult](
    new SelectFilesAction[ParentBackupResult, ChildBackupResult](files),
    new SelectFilesAction[ParentRestoreResult, ChildRestoreResult](files))
