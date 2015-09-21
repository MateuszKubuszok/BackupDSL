package pl.combosolutions.backup.dsl.tasks.beta

object TasksExceptionMessages {

  val CircularDependency = "Declared dependency type must match the actual one"
  val ProxyInitialized = "Implementation is already set"
  val ProxyNotInitialized = "Implementation not set"

  val FakeBuilderWithConfig = "Fake subtask builder should not be used with any configuration"
  val IndependentTaskWithParentConfig = "Independent task cannot rely on any parent task"
  val IndependentTaskWithChildrenConfig = "Independent task cannot rely on any children tasks"
  val ParentDependentWithoutParent = "Parent dependent task require parent definition"
  val ParentDependentWithChildrenConfig = "Parent dependent task cannot rely on any children tasks"
  val ChildrenDependentWithParentConfig = "Child dependent task cannot rely on any parent task"

  val InvalidScriptAction = "Invalid script action"
}
