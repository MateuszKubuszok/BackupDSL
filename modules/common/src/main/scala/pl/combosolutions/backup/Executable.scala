package pl.combosolutions.backup

trait Executable[T <: Executable[T]] extends Serializable {

  def run: Async[Result[T]]

  def digest[U](implicit interpreter: Result[T]#Interpreter[U]): Async[U]
}
