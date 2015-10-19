package pl.combosolutions.backup.psm

import pl.combosolutions.backup.ReportException

object ImplementationPriority extends Enumeration {

  type ImplementationPriority = Value
  val OnlyAllowed, Preferred, Allowed, NotAllowed = Value
}

import ImplementationPriority._

trait ImplementationResolver[Interface] {

  val implementations: Seq[Interface]

  val notFoundMessage: String

  def byFilter(implementation: Interface): Boolean

  def byPriority(implementation: Interface): ImplementationPriority

  final def resolve: Interface = (implementations
    filter byFilter
    filter (byPriority(_) != NotAllowed)
    sortBy byPriority headOption).
    getOrElse(ReportException onIllegalStateOf notFoundMessage)
}
