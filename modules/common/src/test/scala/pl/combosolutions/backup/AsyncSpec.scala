package pl.combosolutions.backup

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import pl.combosolutions.backup.TestExecutionContext.context
import pl.combosolutions.backup.test.Tags.UnitTest

class AsyncSpec extends Specification with Mockito {

  "Async" should {

    "create Async from action" in {
      // given
      val expected = "success"

      // when
      val successResult = Async(Some(expected))
      val failureResult = Async(throw new Throwable)

      // then
      successResult must beSome(expected).await
      failureResult must throwA[Throwable].await
    } tag UnitTest

    "create successful Async from ready value" in {
      // given
      val expected = "success"

      // when
      val result = Async successful Some(expected)

      // then
      result must beSome(expected).await
    } tag UnitTest

    "create successful Async for None result" in {
      // given
      // when
      val result = Async.none[String]

      // then
      result must beNone.await
    } tag UnitTest

    "create successful Async from Option" in {
      // given
      val expected = "success"
      val some = Some(expected)
      val none = None

      // when
      val someResult = Async successful some
      val noneResult = Async successful none

      // then
      someResult must beSome(expected).await
      noneResult must beNone.await
    } tag UnitTest

    "create failed Async from exception" in {
      // given
      val exception = new Throwable

      // when
      val result = Async failed exception

      // then
      result must throwA[Throwable].await
    } tag UnitTest

    "create all-or-none Async sequence from sequence of Async" in {
      // given
      val expected = "success"
      val async1 = Async(Some(expected))
      val async2 = Async(None)
      val async3 = Async(throw new Throwable)
      val someSequence = Seq(async1, async1)
      val noneSequence = Seq(async1, async2)
      val failSequence = Seq(async1, async3)

      // when
      val someResult = Async completeSequence someSequence
      val noneResult = Async completeSequence noneSequence
      val failResult = Async completeSequence failSequence

      // then
      someResult must beSome(Seq(expected, expected)).await
      noneResult must beNone.await
      failResult must throwA[Throwable].await
    } tag UnitTest

    "create all-possible Async sequence from sequence of Async" in {
      // given
      val expected = "success"
      val async1 = Async(Some(expected))
      val async2 = Async(None)
      val async3 = Async(throw new Throwable)
      val someSequence = Seq(async1, async1)
      val noneSequence = Seq(async1, async2)
      val failSequence = Seq(async1, async3)

      // when
      val someResult = Async incompleteSequence someSequence
      val noneResult = Async incompleteSequence noneSequence
      val failResult = Async incompleteSequence failSequence

      // then
      someResult must beSome(Seq(expected, expected)).await
      noneResult must beSome(Seq(expected)).await
      failResult must throwA[Throwable].await
    } tag UnitTest

    "flatMap Async for transformation" in {
      // given
      type ActionType = (Int) => Async[String]
      val async = Async some 1234567890
      val expected = "1234567890"
      val someAction: ActionType = i => Async some i.toString
      val noneAction: ActionType = i => Async.none
      val failAction: ActionType = i => Async failed new Throwable

      // when
      val someResult = Async flatMap (async, someAction)
      val noneResult = Async flatMap (async, noneAction)
      val failResult = Async flatMap (async, failAction)

      // then
      someResult must beSome(expected).await
      noneResult must beNone.await
      failResult must throwA[Throwable].await
    } tag UnitTest

    "map Async for transformation" in {
      // given
      type ActionType = (Int) => String
      val async = Async some 1234567890
      val expected = "1234567890"
      val someAction: ActionType = i => i.toString
      val failAction: ActionType = i => throw new Throwable

      // when
      val someResult = Async map (async, someAction)
      val failResult = Async map (async, failAction)

      // then
      someResult must beSome(expected).await
      failResult must throwA[Throwable].await
    } tag UnitTest
  }
}
