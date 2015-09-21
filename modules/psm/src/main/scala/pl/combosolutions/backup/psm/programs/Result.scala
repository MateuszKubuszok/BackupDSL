package pl.combosolutions.backup.psm.programs

case class Result[T](exitValue: Int, stdout: List[String], stderr: List[String]) {
  type Interpreter[U] = Result[T] => U

  def interpret[U](implicit interpreter: Interpreter[U]): U = interpreter(this)

  def asSpecific[U]: Result[U] = this.asInstanceOf[Result[U]]
}
