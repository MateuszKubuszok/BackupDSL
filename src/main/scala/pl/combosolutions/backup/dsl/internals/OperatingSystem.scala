package pl.combosolutions.backup.dsl.internals

import org.apache.commons.lang3.SystemUtils._

object OperatingSystem {
  val all = Seq(
    // Linux family
    DebianSystem,
    // MacOS family
    MacOSOperatingSystem,
    // Windows family
    Windows7System,
    Windows8System,
    Windows10System
  )

  lazy val current = all filter (_.isCurrent) head
}

// Operating systems families

sealed abstract case class OperatingSystem(name: String, isCurrent: Boolean, isPosix: Boolean, isWindows: Boolean)
abstract case class WindowsSystem(override val name: String, override val isCurrent: Boolean) extends OperatingSystem(name, isCurrent, false, true)
abstract case class PosixSystem(override val name: String, override val isCurrent: Boolean) extends OperatingSystem(name, isCurrent, true, false)

// Linux family

abstract case class LinuxSystem(override val name: String, override val isCurrent: Boolean) extends PosixSystem(name, isCurrent)
case object DebianSystem extends LinuxSystem("Debian", IS_OS_LINUX)

// MacOS family

case object MacOSOperatingSystem extends PosixSystem("MacOS", IS_OS_MAC_OSX)

// Windows family

case object Windows7System extends WindowsSystem("Windows 7", IS_OS_WINDOWS_7)
case object Windows8System extends WindowsSystem("Windows 8", IS_OS_WINDOWS_8)
case object Windows10System extends WindowsSystem("Windows 10", IS_OS_WINDOWS)

// TODO rest of the systems
