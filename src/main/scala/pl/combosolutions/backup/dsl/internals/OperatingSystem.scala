package pl.combosolutions.backup.dsl.internals

import org.apache.commons.lang3.SystemUtils._

import scala.reflect.io.File

import OperatingSystem._

object OperatingSystem {
  val all = Seq(
    // Linux family
    ArchSystem,
    DebianSystem,
    FedoraSystem,
    GentooSystem,
    RedHatSystem,
    // MacOS family
    MacOSXOperatingSystem,
    // Windows family
    Windows7System,
    Windows8System,
    Windows10System
  )

  lazy val current = all filter (_.isCurrent) head

  // from http://linuxmafia.com/faq/Admin/release-files.html
  def IS_OS_ARCH   = File("/etc/arch-release").exists
  def IS_OS_DEBIAN = File("/etc/debian_version").exists
  def IS_OS_FEDORA = File("/etc/fedora-release").exists
  def IS_OS_GENTOO = File("/etc/gentoo-release").exists
  def IS_OS_REDHAT = File("/etc/redhat-release").exists
}

// Operating systems families

sealed abstract case class OperatingSystem(name: String, isCurrent: Boolean, isPosix: Boolean, isWindows: Boolean)
abstract case class WindowsSystem(override val name: String, override val isCurrent: Boolean) extends OperatingSystem(name, isCurrent, false, true)
abstract case class PosixSystem(override val name: String, override val isCurrent: Boolean) extends OperatingSystem(name, isCurrent, true, false)

// Linux family

abstract case class LinuxSystem(override val name: String, override val isCurrent: Boolean) extends PosixSystem(name, isCurrent)
case object ArchSystem extends LinuxSystem("Arch", IS_OS_LINUX && IS_OS_ARCH)
case object DebianSystem extends LinuxSystem("Debian", IS_OS_LINUX && IS_OS_DEBIAN)
case object FedoraSystem extends LinuxSystem("Fedora", IS_OS_LINUX && IS_OS_FEDORA)
case object GentooSystem extends LinuxSystem("Gentoo", IS_OS_LINUX && IS_OS_GENTOO)
case object RedHatSystem extends LinuxSystem("Red Hat", IS_OS_LINUX && IS_OS_REDHAT)

// MacOS family

case object MacOSXOperatingSystem extends PosixSystem("MacOS", IS_OS_MAC_OSX)

// Windows family

case object Windows7System extends WindowsSystem("Windows 7", IS_OS_WINDOWS_7)
case object Windows8System extends WindowsSystem("Windows 8", IS_OS_WINDOWS_8)
case object Windows10System extends WindowsSystem("Windows 10", IS_OS_WINDOWS)

// TODO rest of the systems
