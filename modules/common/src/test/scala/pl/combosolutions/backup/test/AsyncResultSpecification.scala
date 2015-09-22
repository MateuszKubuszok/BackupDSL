package pl.combosolutions.backup.test

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{ Await, Future }

trait AsyncResultSpecification extends Mockito {
  self: Specification with Mockito =>

  def await[T](future: Future[T]) = Await.result(future, Inf)
}
