package pl.combosolutions.backup.tasks

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.test.Tags.UnitTest

class ConfiguratorSpec extends Specification with Mockito {

  "Configurator" should {

    "build current task" in new TestContext {
      // given
      val configurator = new TestConfigurator(Some(parentConfigurator))

      // when
      configurator.buildAllForTest

      // then
      there was one(taskBuilder).build
    }

    "configure tasks with parent present" in new TestContext {
      // given
      val configurator = new TestConfigurator(Some(parentConfigurator))

      // when
      configurator.buildAllForTest

      // then
      there was one(parentConfigurator).addChild(===(configurator.asInstanceOf[TestConfigurator#ParentTaskConfiguratorT#ChildTaskConfiguratorT]))
      there was one(taskBuilder).build
      there was one(taskBuilder).setParent(===(parentBuilder))
    } tag UnitTest

    "configure tasks with child present" in new TestContext {
      // given
      val configurator = new TestConfigurator(None)
      configurator.addChild(childConfigurator.asInstanceOf[TestConfigurator#ChildTaskConfiguratorT])

      // when
      configurator.buildAllForTest

      // then
      there was one(taskBuilder).build
      there was one(childConfigurator).configure
      there was one(taskBuilder).addChild(===(childBuilder))
    } tag UnitTest
  }

  class TestContext extends Scope {

    val parentConfigurator = mock[Configurator[Any, Any, Any, Any, Any, Any]]
    val parentBuilder = mock[TaskBuilder[Any, Any, Any, Any, Any, Any]]
    val childConfigurator = mock[Configurator[Nothing, Any, Any, Nothing, Any, Any]]
    val childBuilder = mock[TaskBuilder[Nothing, Any, Any, Nothing, Any, Any]]
    val backupSubTask = mock[SubTaskBuilder[Any, Any, Any]]
    val restoreSubTask = mock[SubTaskBuilder[Any, Any, Any]]
    val taskBuilder = mock[TestConfigurator#TaskBuilderT]
    parentConfigurator.builder returns parentBuilder
    childConfigurator.builder returns childBuilder

    class TestConfigurator(parent: Option[Configurator[Any, Any, Any, Any, Any, Any]]) extends Configurator(parent) {

      override def builder = taskBuilder

      def buildAllForTest = buildAll
    }
  }
}
