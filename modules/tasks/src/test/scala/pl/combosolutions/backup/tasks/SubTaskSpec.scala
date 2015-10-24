package pl.combosolutions.backup.tasks

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.Async
import pl.combosolutions.backup.test.Tags.UnitTest

import scala.collection.mutable

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
    } tag UnitTest

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
    } tag UnitTest

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
    } tag UnitTest
  }

  "SubTaskProxy" should {

    "foretell implementation's dependency type" in {
      // given
      val independentProxy = new SubTaskProxy[Unit](DependencyType.Independent)
      val parentDependentProxy = new SubTaskProxy[Unit](DependencyType.ParentDependent)
      val childDependentProxy = new SubTaskProxy[Unit](DependencyType.ChildDependent)

      // when
      val independentType = independentProxy.dependencyType
      val parentDependentType = parentDependentProxy.dependencyType
      val childDependentType = childDependentProxy.dependencyType

      // then
      independentType mustEqual DependencyType.Independent
      parentDependentType mustEqual DependencyType.ParentDependent
      childDependentType mustEqual DependencyType.ChildDependent
    } tag UnitTest

    "prevent execution of uninitialized proxy" in {
      // given
      val proxy = new SubTaskProxy[Unit](DependencyType.Independent)

      // when
      // uninitialized

      // then
      proxy.execute must throwA[AssertionError]
    } tag UnitTest

    "prevent wrong implementation setup" in {
      // given
      val subTask = mock[SubTask[Unit]]
      val proxy = new SubTaskProxy[Unit](DependencyType.Independent)
      subTask.dependencyType returns DependencyType.ParentDependent

      // when
      // wrong dependency type set

      // then
      proxy setImplementation subTask must throwA[IllegalArgumentException]
    } tag UnitTest

    "prevent double implementation setup" in {
      // given
      val subTask = mock[SubTask[Unit]]
      val propagation = mutable.Set[Propagator]()
      val proxy = new SubTaskProxy[Unit](DependencyType.Independent)
      subTask.dependencyType returns DependencyType.Independent
      subTask.getPropagation returns propagation

      // when
      proxy setImplementation subTask

      // then
      proxy setImplementation subTask must throwA[AssertionError]
    } tag UnitTest

    "redirect to implementation once set" in {
      // given
      val subTask = mock[SubTask[String]]
      val propagation = mutable.Set[Propagator]()
      val proxy = new SubTaskProxy[String](DependencyType.Independent)
      subTask.dependencyType returns DependencyType.Independent
      subTask.getPropagation returns propagation
      subTask.result returns (Async some "test")

      // when
      proxy setImplementation subTask

      // then
      proxy.result must beSome("test").await
    } tag UnitTest

    "set propagation for implementation" in {
      // given
      val subTask = mock[SubTask[String]]
      val propagator = new Propagator(subTask)
      val propagator2 = new Propagator(subTask)
      val propagation = mutable.Set[Propagator]()
      val proxy = new SubTaskProxy[String](DependencyType.Independent)
      subTask.dependencyType returns DependencyType.Independent
      subTask.getPropagation returns propagation
      subTask.result returns (Async some "test")

      // when
      val result1 = proxy.getPropagation += propagator
      proxy setImplementation subTask
      val result2 = proxy.getPropagation += propagator2

      // then
      result1.toSet mustEqual Set(propagator)
      result2.toSet mustEqual Set(propagator, propagator2)
      result1 must_!= result2
    } tag UnitTest
  }

  "IndependentSubTask" should {

    "be independent" in {
      // given
      val action = () => Async some "unimportant"

      // when
      val subTask = new IndependentSubTask(action)

      // then
      subTask.dependencyType mustEqual DependencyType.Independent
    } tag UnitTest

    "run action" in {
      // given
      val expected = "test-result"
      val action = () => Async some expected

      // when
      val subTask = new IndependentSubTask(action)

      // then
      subTask.result must beSome(expected).await
    } tag UnitTest

    "propagate result" in {
      // given
      val expected = "expected"
      val action = () => Async some expected
      val subTask = new IndependentSubTask(action)
      val action2 = (str: String) => Async some s"$str$str"
      val subTask2 = new ParentDependentSubTask(action2, subTask)
      subTask.getPropagation += subTask2.propagator

      // when
      subTask.result
      val result = subTask2.result

      // then
      result must beSome(expected + expected).await
    } tag UnitTest
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
    } tag UnitTest

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
    } tag UnitTest
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
    } tag UnitTest

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
    } tag UnitTest
  }
}
