package pl.combosolutions.backup.dsl

object ReportException {

  def onIllegalStateOf(message: String) = throw new IllegalStateException(message)
  def onIllegalStateOf(message: String, exception: Throwable) = throw new IllegalStateException(message, exception)

  def onNotImplemented(message: String) = throw new NotImplementedError(message)

  def onToDoCodeIn(clazz: Class[_]) = throw new NotImplementedError(s"TODO code in ${clazz.getName}")
}
