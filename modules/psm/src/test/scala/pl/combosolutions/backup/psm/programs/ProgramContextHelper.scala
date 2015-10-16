package pl.combosolutions.backup.psm.programs

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import pl.combosolutions.backup.{ Cleaner, Async, Result }
import pl.combosolutions.backup.psm.elevation.ObligatoryElevationMode

import scala.reflect.ClassTag

trait ProgramContextHelper {
  this: Specification with Mockito =>

  class ProgramContext[ProgramType <: Program[ProgramType], ResultType](
      programClass: Class[ProgramType],
      resultClass:  Class[ResultType]
  ) extends Scope {

    type InterpreterType = Result[ProgramType]#Interpreter[ResultType]

    implicit val programTag: ClassTag[ProgramType] = ClassTag(programClass)
    implicit val resultTag: ClassTag[InterpreterType] = ClassTag(classOf[InterpreterType])

    val program = mock[Program[ProgramType]]
    val elevationMode = mock[ObligatoryElevationMode]
    val cleaner = new Cleaner {}

    elevationMode[ProgramType](any[ProgramType], ===(cleaner)) returns program

    def makeDigestReturn(result: ResultType): Unit =
      program.digest[ResultType](any[InterpreterType]) returns Async.some(result)
  }
}
