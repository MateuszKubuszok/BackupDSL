package pl.combosolutions.backup.test

import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{ Await, Future }

trait AsyncSpecificationHelper {
  self: Specification =>

  def await[T](future: Future[T]) = Await.result(future, Inf)
}
