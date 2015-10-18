package pl.combosolutions.backup

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.DefaultsAndConstants._
import pl.combosolutions.backup.test.Tags.UnitTest

import scala.concurrent.ExecutionContext

class ExecutionContextsSpec extends Specification with Mockito {

  "ExecutionContexts" should {

    "initialize contexts to None" in {
      // given
      // when
      val context = new ExecutionContexts {}

      // then
      context.commandProxy.executionContext must beNone
      context.programProxy.executionContext must beNone
      context.taskProxy.executionContext must beNone
    } tag UnitTest

    "set context when ordered to" in {
      // given
      val context = new ExecutionContexts {}

      // when
      context setCommandSize 10
      context setProgramSize 10
      context setTaskSize 10

      // then
      context.commandProxy.executionContext must beSome
      context.programProxy.executionContext must beSome
      context.taskProxy.executionContext must beSome
    } tag UnitTest

    "in the end contains accurate contexts" in {
      // given

      // when
      val context = ExecutionContexts

      // then
      context.Command.context mustEqual context.commandProxy
      context.Program.context mustEqual context.programProxy
      context.Task.context mustEqual context.taskProxy
    } tag UnitTest
  }

  "ExecutionContextProxy" should {

    "execute Runnables using underlying implementation" in {
      // given
      val context = new ExecutionContexts {}
      val implementation = mock[ExecutionContext]
      val runnable1 = new Runnable {
        override def run(): Unit = {}
      }
      val runnable2 = new Runnable {
        override def run(): Unit = {}
      }
      context.commandProxy.setExecutionContextTo(implementation)

      // when
      context.commandProxy execute runnable1
      context.programProxy execute runnable2

      // then
      there was one(implementation).execute(===(runnable1))
      there was no(implementation).execute(===(runnable2))
    } tag UnitTest

    "report Throwables using underlying implementation" in {
      // given
      val context = new ExecutionContexts {}
      val implementation = mock[ExecutionContext]
      val throwable1 = new Throwable
      val throwable2 = new Throwable
      context.commandProxy.setExecutionContextTo(implementation)

      // when
      context.commandProxy reportFailure throwable1
      context.programProxy reportFailure throwable2

      // then
      there was one(implementation).reportFailure(===(throwable1))
      there was no(implementation).reportFailure(===(throwable2))
    } tag UnitTest
  }

  "DefaultECVPoolSizes" should {

    "use values from DefaultsAndConstants" in {
      // given
      // when
      val defaults = new ExecutionContexts with DefaultECVPoolSizes

      // then
      defaults.defaultCommandThreadPoolSize mustEqual CommandThreadPoolSize
      defaults.defaultProgramThreadPoolSize mustEqual ProgramThreadPoolSize
      defaults.defaultTaskThreadPoolSize mustEqual TaskThreadPoolSize
    } tag UnitTest

    "sets values" in {
      // given
      // when
      val defaults = new ExecutionContexts with DefaultECVPoolSizes

      // then
      defaults.commandProxy.executionContext must beSome
      defaults.programProxy.executionContext must beSome
      defaults.taskProxy.executionContext must beSome
    } tag UnitTest
  }
}
