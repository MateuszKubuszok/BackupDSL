package pl.combosolutions.backup.psm.commands

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.{ Cleaner, Async, Result }
import pl.combosolutions.backup.psm.elevation.ObligatoryElevationMode

import scala.reflect.ClassTag

trait CommandContextHelper {
  this: Specification with Mockito =>

  class CommandContext[CommandType <: Command[CommandType], ResultType](
      programClass: Class[CommandType],
      resultClass:  Class[ResultType]
  ) extends Scope {

    type InterpreterType = Result[CommandType]#Interpreter[ResultType]

    implicit val commandTag: ClassTag[CommandType] = ClassTag(programClass)
    implicit val resultTag: ClassTag[InterpreterType] = ClassTag(classOf[InterpreterType])

    val command = mock[Command[CommandType]]
    val elevationMode = mock[ObligatoryElevationMode]
    val cleaner = new Cleaner {}

    elevationMode[CommandType](any[CommandType], ===(cleaner)) returns command

    def makeDigestReturn(result: ResultType): Unit =
      command.digest[ResultType](any[InterpreterType]) returns Async.some(result)
  }
}
