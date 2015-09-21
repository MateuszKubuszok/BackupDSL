package pl.combosolutions.backup.psm.systems

import java.nio.file.{ Files, Paths }

import org.apache.commons.lang3.SystemUtils._

import OperatingSystem._

object OperatingSystem {

  // from http://linuxmafia.com/faq/Admin/release-files.html
  private[systems] def IS_OS_ARCH = Files.exists(Paths.get("/etc/arch-release"))
  private[systems] def IS_OS_DEBIAN = Files.exists(Paths.get("/etc/debian_version"))
  private[systems] def IS_OS_FEDORA = Files.exists(Paths.get("/etc/fedora-release"))
  private[systems] def IS_OS_GENTOO = Files.exists(Paths.get("/etc/gentoo-release"))
  private[systems] def IS_OS_REDHAT = Files.exists(Paths.get("/etc/redhat-release"))
}

// Operating systems families

sealed abstract class OperatingSystem(val name: String, val isCurrent: Boolean, val isPosix: Boolean, val isWindows: Boolean)
sealed abstract class WindowsSystem(name: String, isCurrent: Boolean) extends OperatingSystem(name, isCurrent, false, true)
sealed abstract class PosixSystem(name: String, isCurrent: Boolean) extends OperatingSystem(name, isCurrent, true, false)

// Linux family

sealed abstract class LinuxSystem(name: String, isCurrent: Boolean) extends PosixSystem(name, isCurrent)
case object ArchSystem extends LinuxSystem("Arch", IS_OS_LINUX && IS_OS_ARCH)
case object DebianSystem extends LinuxSystem("Debian", IS_OS_LINUX && IS_OS_DEBIAN)
case object FedoraSystem extends LinuxSystem("Fedora", IS_OS_LINUX && IS_OS_FEDORA)
case object GentooSystem extends LinuxSystem("Gentoo", IS_OS_LINUX && IS_OS_GENTOO)
case object RedHatSystem extends LinuxSystem("Red Hat", IS_OS_LINUX && IS_OS_REDHAT)
case object GenericLinuxSystem extends LinuxSystem("Generic Linux", IS_OS_LINUX)

// MacOS family

case object GenericMacOSXOperatingSystem extends PosixSystem("MacOS", IS_OS_MAC_OSX)
case object GenericMacOperatingSystem extends PosixSystem("MacOS", IS_OS_MAC)

// Windows family

case object Windows95System extends WindowsSystem("Windows 7", IS_OS_WINDOWS_95)
case object Windows98System extends WindowsSystem("Windows 7", IS_OS_WINDOWS_98)
case object WindowsMESystem extends WindowsSystem("Windows 7", IS_OS_WINDOWS_ME)
case object WindowsNTSystem extends WindowsSystem("Windows 7", IS_OS_WINDOWS_NT)
case object WindowsXPSystem extends WindowsSystem("Windows 7", IS_OS_WINDOWS_XP)
case object WindowsVistaSystem extends WindowsSystem("Windows 7", IS_OS_WINDOWS_VISTA)
case object Windows7System extends WindowsSystem("Windows 7", IS_OS_WINDOWS_7)
case object Windows8System extends WindowsSystem("Windows 8", IS_OS_WINDOWS_8)
case object GenericWindowsSystem extends WindowsSystem("Windows 10", IS_OS_WINDOWS)

// Unknown system

case object UnknownSystem extends OperatingSystem("Unknown", true, false, false)

// TODO add the rest of systems

trait OperatingSystemComponent {

  def operatingSystem: OperatingSystem
}

trait OperatingSystemComponentImpl extends OperatingSystemComponent {

  override val operatingSystem = Seq(
    // Linux family
    ArchSystem,
    DebianSystem,
    FedoraSystem,
    GentooSystem,
    RedHatSystem,
    GenericLinuxSystem,

    // MacOS family
    GenericMacOSXOperatingSystem,
    GenericMacOperatingSystem,

    // Windows family
    Windows95System,
    Windows98System,
    WindowsMESystem,
    WindowsNTSystem,
    WindowsXPSystem,
    WindowsVistaSystem,
    Windows7System,
    Windows8System,
    GenericWindowsSystem,

    // Unknown system
    UnknownSystem
  ) find (_.isCurrent) get
}
