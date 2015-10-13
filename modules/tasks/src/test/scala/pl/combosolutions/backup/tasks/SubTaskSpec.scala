package pl.combosolutions.backup.tasks

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.Async

class SubTaskSpec extends Specification with Mockito {

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
}
