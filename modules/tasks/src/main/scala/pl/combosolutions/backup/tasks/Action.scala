package pl.combosolutions.backup.tasks

object Action extends Enumeration {

  type Action = Value
  val No, Backup, Restore = Value

  class ActionRead extends scopt.Read[Action] {
    override def arity: Int = 1

    override def reads: (String) => Action = (action) => action.trim.toLowerCase match {
      case "backup" => Backup
      case "restore" => Restore
      case _ => No
    }
  }

  implicit val string2Action = new ActionRead
}

