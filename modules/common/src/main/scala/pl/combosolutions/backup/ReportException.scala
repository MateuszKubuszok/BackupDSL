package pl.combosolutions.backup

object ReportException {

  def onIllegalArgumentOf(message: String): Nothing = throw new IllegalArgumentException(message)

  def onIllegalStateOf(message: String): Nothing = throw new IllegalStateException(message)
  def onIllegalStateOf(message: String, exception: Throwable): Nothing =
    throw new IllegalStateException(message, exception)

  def onNotImplemented(message: String): Nothing = throw new NotImplementedError(message)

  def onToDoCodeIn(clazz: Class[_]): Nothing = throw new NotImplementedError(s"TODO code in ${clazz.getName}")
}
