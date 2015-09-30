package pl.combosolutions.backup.psm

import pl.combosolutions.backup.ReportException
import pl.combosolutions.backup.psm.ImplementationPriority.ImplementationPriority

object ImplementationPriority extends Enumeration {
  type ImplementationPriority = Value
  val OnlyAllowed, Preferred, Allowed, NotAllowed = Value
}

trait ImplementationResolver[Interface] {

  val implementations: Seq[Interface]

  val notFoundMessage: String

  def byFilter(implementation: Interface): Boolean

  def byPriority(implementation: Interface): ImplementationPriority

  final def resolve = (implementations filter byFilter sortBy byPriority headOption).
    getOrElse(ReportException onIllegalStateOf notFoundMessage)
}
