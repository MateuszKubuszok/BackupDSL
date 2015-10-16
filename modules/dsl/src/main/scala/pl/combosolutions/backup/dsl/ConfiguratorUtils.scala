package pl.combosolutions.backup.dsl

trait ConfiguratorUtils[C <: ConfiguratorUtils[C]] {
  self: C =>

  def forThis(block: C => Unit): Unit = block(this)

  object configure {

    def apply(f: () => Unit) = new configure {

      override def apply(): Unit = f()
    }
  }

  trait configure {

    def apply(): Unit
  }
}
