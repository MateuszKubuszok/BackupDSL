package pl.combosolutions.backup.dsl.internals.operations

import pl.combosolutions.backup.dsl.internals.programs.Program

trait PlatformSpecificElevation {

  val elevationAvailable: Boolean

  val elevationCMD: String

  val elevationArgs: List[String] = List()

  def elevateDirect[T <: Program[T]](program: Program[T]): Program[T]

  def elevateRemote[T <: Program[T]](program: Program[T], cleaner: Cleaner): Program[T]
}
