package pl.combosolutions.backup.dsl.internals.operations

trait PlatformSpecificElevation {
  val elevationAvailable: Boolean

  def elevate[T <: Program[T]](program: Program[T]): Program[T]
}
