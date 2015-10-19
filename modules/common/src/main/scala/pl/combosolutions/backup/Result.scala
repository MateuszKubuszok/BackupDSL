package pl.combosolutions.backup

import scala.annotation.implicitNotFound

case class Result[T](exitValue: Int, stdout: List[String], stderr: List[String]) {

  @implicitNotFound("No implicit interpretation of Result[T] into type U found")
  type Interpreter[U] = Result[T] => U

  def interpret[U](implicit interpreter: Interpreter[U]): U = interpreter(this)

  def asSpecific[U]: Result[U] = this.asInstanceOf[Result[U]]
}
