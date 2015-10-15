package pl.combosolutions.backup.tasks

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.Async

class SubTaskSpec extends Specification with Mockito {

  "SubTask" should {

    "create subtask from function" in {
      // given
      val expected = "test-string"
      val action = () => Async some expected

      // when
      val subTask = SubTask(action)

      // then
      subTask.result must beSome(expected).await
    }

    "flatMap result" in {
      // given
      val subTask = new TestSubTask[String] {
        override val dependencyType = DependencyType.Independent
        override def execute = Async some "test-result"
      }

      // when
      val flatMappedTask = subTask flatMap (_ => SubTask(() => Async.none[String]))

      // then
      flatMappedTask.result must beNone.await
    }

    "map result" in {
      // given
      val initial = "test-result"
      val suffix = " test-suffix"
      val subTask = new TestSubTask[String] {
        override val dependencyType = DependencyType.Independent
        override def execute = Async some initial
      }
      val expected = initial ++ suffix

      // when
      val mappedTask = subTask map (_ ++ suffix)

      // then
      mappedTask.result must beSome(expected).await
    }
  }

  "SubTaskProxy" should {

    "foretell implementation's dependency type" in {
      // given
      val indepedentProxy = new SubTaskProxy[Unit](DependencyType.Independent)
      val parentDepedentProxy = new SubTaskProxy[Unit](DependencyType.ParentDependent)
      val childDepedentProxy = new SubTaskProxy[Unit](DependencyType.ChildDependent)

      // when
      val independentType = indepedentProxy.dependencyType
      val parentDependentType = parentDepedentProxy.dependencyType
      val childDependentType = childDepedentProxy.dependencyType

      // then
      independentType mustEqual DependencyType.Independent
      parentDependentType mustEqual DependencyType.ParentDependent
      childDependentType mustEqual DependencyType.ChildDependent
    }

    "prevent execution of uninitialized proxy" in {
      // given
      val proxy = new SubTaskProxy[Unit](DependencyType.Independent)

      // when
      // uninitialized

      // then
      proxy.execute must throwA[AssertionError]
    }

    "prevent wrong implementation setup" in {
      // given
      val subTask = mock[SubTask[Unit]]
      val proxy = new SubTaskProxy[Unit](DependencyType.Independent)
      subTask.dependencyType returns DependencyType.ParentDependent

      // when
      // wrong dependency type set

      // then
      proxy setImplementation subTask must throwA[IllegalArgumentException]
    }

    "prevent double implementation setup" in {
      // given
      val subTask = mock[SubTask[Unit]]
      val proxy = new SubTaskProxy[Unit](DependencyType.Independent)
      subTask.dependencyType returns DependencyType.Independent

      // when
      proxy setImplementation subTask

      // then
      proxy setImplementation subTask must throwA[AssertionError]
    }

    "redirect to implementation once set" in {
      // given
      val subTask = mock[SubTask[String]]
      val proxy = new SubTaskProxy[String](DependencyType.Independent)
      subTask.dependencyType returns DependencyType.Independent
      subTask.result returns (Async some "test")

      // when
      proxy setImplementation subTask

      // then
      proxy.result must beSome("test").await
    }
  }

  "IndependentSubTask" should {

    "be independent" in {
      // given
      val action = () => Async some "unimportant"

      // when
      val subTask = new IndependentSubTask(action)

      // then
      subTask.dependencyType mustEqual DependencyType.Independent
    }

    "run action" in {
      // given
      val expected = "test-result"
      val action = () => Async some expected

      // when
      val subTask = new IndependentSubTask(action)

      // then
      subTask.result must beSome(expected).await
    }
  }

  "ParentDependentSubTask" should {

    "be parent dependent" in {
      // given
      val parent = mock[SubTask[String]]
      val action = (string: String) => Async some string

      // when
      val subTask = new ParentDependentSubTask[String, String](action, parent)

      // then
      subTask.dependencyType mustEqual DependencyType.ParentDependent
    }

    "run action" in {
      // given
      val expected = "test-result"
      val parent = mock[SubTask[String]]
      val action = (string: String) => Async some string
      parent.result returns (Async some expected)

      // when
      val subTask = new ParentDependentSubTask[String, String](action, parent)

      // then
      subTask.result must beSome(expected).await
    }
  }

  "ChildDependentSubTask" should {

    "be child dependent" in {
      // given
      val child = mock[SubTask[String]]
      val action = (strings: Traversable[String]) => Async some strings.headOption.getOrElse("")

      // when
      val subTask = new ChildDependentSubTask[String, String](action, Seq(child))

      // then
      subTask.dependencyType mustEqual DependencyType.ChildDependent
    }

    "run action" in {
      // given
      val expected = "test-result"
      val child = mock[SubTask[String]]
      val action = (strings: Traversable[String]) => Async some strings.headOption.getOrElse("")
      child.result returns (Async some expected)

      // when
      val subTask = new ChildDependentSubTask[String, String](action, Seq(child))

      // then
      subTask.result must beSome(expected).await
    }
  }
}
