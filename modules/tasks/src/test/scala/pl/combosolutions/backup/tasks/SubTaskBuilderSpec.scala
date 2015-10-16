package pl.combosolutions.backup.tasks

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.Async

import scala.collection.mutable

class SubTaskBuilderSpec extends Specification with Mockito {

  "FakeSubTaskBuilder" should {

    "set passed subTask inside injectable proxy" in {
      // given
      val expected = "test-subtask"
      val subTask = mock[SubTask[String]]
      val propagation = mutable.Set[Propagator]()
      subTask.dependencyType returns DependencyType.Independent
      subTask.getPropagation returns propagation
      subTask.result returns (Async some expected)

      // when
      val builder = new FakeSubTaskBuilder[String, Unit, Unit](subTask, DependencyType.Independent)

      // then
      builder.injectableProxy.result must beSome(expected).await
    }

    "prevent configuration for parent" in {
      // given
      val subTask = mock[SubTask[String]]
      val propagation = mutable.Set[Propagator]()
      subTask.dependencyType returns DependencyType.Independent
      subTask.getPropagation returns propagation
      val parent = mock[SubTaskBuilder[Unit, _, _]]

      // when
      val builder = new FakeSubTaskBuilder[String, Unit, Unit](subTask, DependencyType.Independent)

      // then
      builder configureForParent parent must throwA[IllegalArgumentException]
    }

    "prevent configuration for children" in {
      // given
      val subTask = mock[SubTask[String]]
      val propagation = mutable.Set[Propagator]()
      subTask.dependencyType returns DependencyType.Independent
      subTask.getPropagation returns propagation
      val child = mock[SubTaskBuilder[Unit, _, _]]

      // when
      val builder = new FakeSubTaskBuilder[String, Unit, Unit](subTask, DependencyType.Independent)

      // then
      builder configureForChildren Seq(child) must throwA[IllegalArgumentException]
    }
  }

  "IndependentSubTaskBuilder" should {

    "spawn Independent subtask" in {
      // given
      val action = () => Async some "unimportant"

      // when
      val builder = new IndependentSubTaskBuilder[String, Unit, Unit](action)

      // then
      builder.injectableProxy.dependencyType mustEqual DependencyType.Independent
    }

    "be independent" in {
      // given
      val action = () => Async some "unimportant"
      val parent = mock[SubTaskBuilder[Unit, Unit, Unit]]
      val child = mock[SubTaskBuilder[Unit, Unit, Unit]]

      // when
      val builder = new IndependentSubTaskBuilder[String, Unit, Unit](action)

      // then
      builder.configureForParent(parent) must throwA[IllegalArgumentException]
      builder.configureForChildren(List(child)) must throwA[IllegalArgumentException]
    }
  }

  "ParentDependentSubTaskBuilder" should {

    "spawn ParentDependent subtask" in {
      // given
      val action = (_: String) => Async some "unimportant"

      // when
      val builder = new ParentDependentSubTaskBuilder[String, String, Unit](action)

      // then
      builder.injectableProxy.dependencyType mustEqual DependencyType.ParentDependent
    }

    "be parent dependent" in {
      // given
      val action = (_: String) => Async some "unimportant"
      val parent = mock[SubTaskBuilder[String, Unit, Unit]]
      val child = mock[SubTaskBuilder[Unit, Unit, Unit]]

      // when
      val builder = new ParentDependentSubTaskBuilder[String, String, Unit](action)

      // then
      builder.configureForParent(parent) must not(throwA[IllegalArgumentException])
      builder.configureForChildren(List(child)) must throwA[IllegalArgumentException]
    }
  }

  "ChildDependentSubTaskBuilder" should {

    "spawn ParentDependent subtask" in {
      // given
      val action = (_: Traversable[String]) => Async some "unimportant"

      // when
      val builder = new ChildDependentSubTaskBuilder[String, Unit, String](action)

      // then
      builder.injectableProxy.dependencyType mustEqual DependencyType.ChildDependent
    }

    "be child dependent" in {
      // given
      val action = (_: Traversable[String]) => Async some "unimportant"
      val parent = mock[SubTaskBuilder[Unit, Unit, Unit]]
      val child = mock[SubTaskBuilder[String, Unit, Unit]]

      // when
      val builder = new ChildDependentSubTaskBuilder[String, Unit, String](action)

      // then
      builder.configureForParent(parent) must throwA[IllegalArgumentException]
      builder.configureForChildren(List(child)) must not(throwA[IllegalArgumentException])
    }
  }
}
