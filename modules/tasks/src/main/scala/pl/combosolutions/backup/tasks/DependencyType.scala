package pl.combosolutions.backup.tasks

object DependencyType extends Enumeration {
  type DependencyType = Value
  val Independent, ParentDependent, ChildDependent = Value
}
