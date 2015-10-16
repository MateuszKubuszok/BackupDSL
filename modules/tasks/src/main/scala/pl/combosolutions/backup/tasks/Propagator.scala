package pl.combosolutions.backup.tasks

import pl.combosolutions.backup.Logging

class Propagator(target: SubTask[_]) extends Logging {

  final def apply(): Unit = propagation

  private lazy val propagation = {
    logger trace s"Propagate information about finished future to $target"
    target.result
    logger trace s"$target called"
  }
}
