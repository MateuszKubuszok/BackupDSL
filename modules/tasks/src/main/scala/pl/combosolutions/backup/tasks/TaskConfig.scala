package pl.combosolutions.backup.tasks

case class TaskConfig[BR, PBR, CBR, RR, PRR, CRR](
    parent:   Option[TaskBuilder[PBR, _, BR, PRR, _, RR]]      = None,
    children: Traversable[TaskBuilder[CBR, BR, _, CRR, RR, _]] = Seq()
) {

  type TaskConfigT = TaskConfig[BR, PBR, CBR, RR, PRR, CRR]
  type TaskBuilderT = TaskBuilder[BR, PBR, CBR, RR, PRR, CRR]

  def setParent(parent: TaskBuilderT#ParentTaskBuilderT): TaskConfigT = copy(parent = Some(parent))

  def addChild(child: TaskBuilderT#ChildTaskBuilderT): TaskConfigT = copy(children = children ++ Seq(child))
}
