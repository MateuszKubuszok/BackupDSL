package pl.combosolutions.backup.dsl.internals.operations.posix.linux

import pl.combosolutions.backup.dsl.internals.operations.posix.WhichProgram
import pl.combosolutions.backup.dsl.internals.operations.posix.PosixPrograms._
import pl.combosolutions.backup.dsl.internals.operations.{PlatformSpecificElevation, Program}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object GKSudoElevation extends PlatformSpecificElevation {
  override lazy val elevationAvailable: Boolean =
    Await.result(WhichProgram("gksudo").digest[Boolean], Duration.Inf) getOrElse false

  override def elevate[T <: Program[T]](program: Program[T]) = new Program[T]("gksudo", program.name :: program.arguments)
}

object KDESudoElevation extends PlatformSpecificElevation {
  override lazy val elevationAvailable: Boolean =
    Await.result(WhichProgram("kdesudo").digest[Boolean], Duration.Inf) getOrElse false

  override def elevate[T <: Program[T]](program: Program[T]) = new Program[T]("kdesudo", program.name :: program.arguments)
}

