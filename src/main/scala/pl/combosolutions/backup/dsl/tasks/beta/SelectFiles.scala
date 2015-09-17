package pl.combosolutions.backup.dsl.tasks.beta

import java.nio.file.{ Paths, Path }

import pl.combosolutions.backup.dsl.AsyncResult
import pl.combosolutions.backup.dsl.tasks.beta.SelectFiles.SelectFilesAction
import pl.combosolutions.backup.dsl.internals.ExecutionContexts.Task.context

object SelectFiles {

  private def selectFilesAction(files: List[String]): () => AsyncResult[List[Path]] = () => AsyncResult { () =>
    files map (Paths get _)
  }

  class SelectFilesAction[ParentResult, ChildResult](files: List[String])
    extends IndependentSubTaskBuilder[List[Path], ParentResult, ChildResult](selectFilesAction(files))
}

class SelectFiles[ParentBackupResult, ChildBackupResult, ParentRestoreResult, ChildRestoreResult](files: List[String])
  extends TaskBuilder[List[Path], ParentBackupResult, ChildBackupResult, List[Path], ParentRestoreResult, ChildRestoreResult](
    new SelectFilesAction[ParentBackupResult, ChildBackupResult](files),
    new SelectFilesAction[ParentRestoreResult, ChildRestoreResult](files))
