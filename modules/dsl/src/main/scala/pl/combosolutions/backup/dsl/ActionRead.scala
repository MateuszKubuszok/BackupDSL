package pl.combosolutions.backup.dsl

import pl.combosolutions.backup.tasks.Action._

class ActionRead extends scopt.Read[Action] {
  override def arity: Int = 1

  override def reads: (String) => Action = (action) => action.trim.toLowerCase match {
    case "backup"  => Backup
    case "restore" => Restore
    case _         => No
  }
}

object ActionRead {

  implicit val string2Action = new ActionRead
}
