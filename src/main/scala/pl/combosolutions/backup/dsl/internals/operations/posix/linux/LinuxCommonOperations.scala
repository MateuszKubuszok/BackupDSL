package pl.combosolutions.backup.dsl.internals.operations.posix.linux

import pl.combosolutions.backup.dsl.internals.operations.Program
import pl.combosolutions.backup.dsl.internals.operations.posix.PosixCommonOperations

abstract trait LinuxCommonOperations extends PosixCommonOperations {
  lazy val useGnome = false // TODO
  lazy val useKDE   = false // TODO
  lazy val elevator = if      (useGnome) "gksudo"
                      else if (useKDE)   "kdesudo"
                      else throw new InternalError("Required some GUI command elevation")

  override def elevate[T](program: Program[T]) = Program[T](elevator, program.name :: program.arguments)
}
