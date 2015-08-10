package pl.combosolutions.backup.dsl.internals.operations

trait PlatformSpecificElevation {
  val elevationAvailable: Boolean

  val elevationCMD: String

  def elevateDirect[T <: Program[T]](program: Program[T]): Program[T]

  def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T]
}
