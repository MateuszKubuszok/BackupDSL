package pl.combosolutions.backup.dsl

trait ConfiguratorUtils[C <: ConfiguratorUtils[C]] {
  self: C =>

  def forThis(block: C => Unit): Unit = block(this)
}
