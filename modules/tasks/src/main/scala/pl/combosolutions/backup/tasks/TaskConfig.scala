package pl.combosolutions.backup.tasks

case class TaskConfig[BR, PBR, CBR, RR, PRR, CRR](
    parent:   Option[TaskBuilder[PBR, _, _, PRR, _, _]]      = None,
    children: Traversable[TaskBuilder[CBR, _, _, CRR, _, _]] = Seq()
) {

  type TaskConfigT = TaskConfig[BR, PBR, CBR, RR, PRR, CRR]
  type TaskBuilderT = TaskBuilder[BR, PBR, CBR, RR, PRR, CRR]

  def setParent(parent: TaskBuilderT#ParentTaskBuilderT): TaskConfigT = copy(parent = Some(parent))

  def addChild(child: TaskBuilderT#ChildTaskBuilderT): TaskConfigT = copy(children = children ++ Seq(child))
}
