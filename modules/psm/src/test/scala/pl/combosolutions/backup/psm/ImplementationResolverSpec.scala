package pl.combosolutions.backup.psm

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.test.Tags.UnitTest

class ImplementationResolverSpec extends Specification with Mockito {

  "ImplementationResolver" should {

    "find first top-level implementation" in {
      // given
      val implementations = Seq(
        new TestTrait(false, Preferred),
        new TestTrait(true, Allowed),
        new TestTrait(true, Preferred)
      )
      val resolver = new TestImplementationResolver(implementations)

      // when
      val result = resolver.resolve

      // then
      result mustEqual implementations(2)
    } tag UnitTest

    "report error when no implementation available" in {
      // given
      val implementations = Seq(
        new TestTrait(false, OnlyAllowed),
        new TestTrait(false, Allowed),
        new TestTrait(false, Preferred),
        new TestTrait(true, NotAllowed)
      )
      val resolver = new TestImplementationResolver(implementations)

      // when
      // then
      resolver.resolve must throwA[IllegalStateException]
    } tag UnitTest
  }

  class TestTrait(val filter: Boolean, val priority: ImplementationPriority)

  class TestImplementationResolver(override val implementations: Seq[TestTrait])
      extends ImplementationResolver[TestTrait] {

    val notFoundMessage: String = ""

    def byFilter(implementation: TestTrait): Boolean = implementation.filter

    def byPriority(implementation: TestTrait): ImplementationPriority =
      implementation.priority
  }
}
