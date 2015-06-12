package pl.combosolutions.backup.dsl.internals

import scala.collection.mutable.{MutableList}
import scala.concurrent.Future
import scala.util.{Try, Failure, Success}

case class Result(exitValue: Int, stdout: List[String], stderr: List[String])

trait ProgramRunner {
  import sys.process._

  def runProgram(name: String, arguments: String*) = Future {
    Try {
      var stdout = MutableList[String]()
      var stderr = MutableList[String]()

      val execution = Process(s"$name ${arguments.reduce(_ + " " + _)}") run ProcessLogger(stdout += _, stderr += _)
      val exitValue = execution exitValue

      Result(exitValue, stdout toList, stderr toList)
    } match {
      case Success(result) => Some(result)
      case Failure => None
    }
  }
}
