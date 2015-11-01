package pl.combosolutions.backup.tasks

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.Async
import pl.combosolutions.backup.test.Tags.UnitTest

class TaskSpec extends Specification with Mockito {

  "Task" should {

    "execute backup subtask" in new TestContext {
      // given
      // when
      val result = task.backup

      // then
      result must beSome(backupExpected).await
    } tag UnitTest

    "execute restore subtask" in new TestContext {
      // given
      // when
      val result = task.restore

      // then
      result must beSome(restoreExpected).await
    } tag UnitTest

    "execute depending on action" in new TestContext {
      // given
      type ResultType = Either[String, String]
      val rightExpected: ResultType = Right(backupExpected)
      val leftExpected: ResultType = Left(restoreExpected)

      // when
      val resultBackup = task.eitherResult(Action.Backup)
      val resultRestore = task.eitherResult(Action.Restore)

      // then
      resultBackup must beSome(rightExpected).await
      resultRestore must beSome(leftExpected).await
      task.eitherResult(Action.No) must throwA[IllegalStateException]
    } tag UnitTest
  }

  class TestContext extends Scope {

    val backupExpected = "backup"
    val backupAction = () => Async some backupExpected
    val backup = new IndependentSubTask[String](backupAction)
    val restoreExpected = "restore"
    val restoreAction = () => Async some restoreExpected
    val restore = new IndependentSubTask[String](restoreAction)
    val task = new Task(backup, restore)
  }
}
