package pl.combosolutions.backup.psm.systems

import java.nio.file.{ Files, Paths }

import org.apache.commons.lang3.SystemUtils._

import pl.combosolutions.backup.psm.ImplementationPriority._
import pl.combosolutions.backup.psm.ImplementationResolver
import pl.combosolutions.backup.psm.PsmExceptionMessages.NoOperatingSystemAvailable

import OperatingSystem._
import OperatingSystemComponentImpl.resolve

object OperatingSystem {

  // from http://linuxmafia.com/faq/Admin/release-files.html
  private[systems] def isOSArch = Files.exists(Paths.get("/etc/arch-release"))
  private[systems] def isOSDebian = Files.exists(Paths.get("/etc/debian_version"))
  private[systems] def isOSFedora = Files.exists(Paths.get("/etc/fedora-release"))
  private[systems] def isOSGentoo = Files.exists(Paths.get("/etc/gentoo-release"))
  private[systems] def isOSRedHat = Files.exists(Paths.get("/etc/redhat-release"))
}

// Operating systems families

sealed abstract class OperatingSystem(
  val name:      String,
  val isCurrent: Boolean,
  val isPosix:   Boolean,
  val isWindows: Boolean
)
sealed abstract class WindowsSystem(
  name:      String,
  isCurrent: Boolean
) extends OperatingSystem(name, isCurrent, false, true)
sealed abstract class PosixSystem(
  name:      String,
  isCurrent: Boolean
) extends OperatingSystem(name, isCurrent, true, false)

// Linux family

sealed abstract class LinuxSystem(name: String, isCurrent: Boolean) extends PosixSystem(name, isCurrent)
case object ArchSystem extends LinuxSystem("Arch", IS_OS_LINUX && isOSArch)
case object DebianSystem extends LinuxSystem("Debian", IS_OS_LINUX && isOSDebian)
case object FedoraSystem extends LinuxSystem("Fedora", IS_OS_LINUX && isOSFedora)
case object GentooSystem extends LinuxSystem("Gentoo", IS_OS_LINUX && isOSGentoo)
case object RedHatSystem extends LinuxSystem("Red Hat", IS_OS_LINUX && isOSRedHat)
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

object OperatingSystemComponentImpl extends ImplementationResolver[OperatingSystem] {

  override lazy val implementations = Seq(
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
  )

  override lazy val notFoundMessage = NoOperatingSystemAvailable

  override def byFilter(system: OperatingSystem): Boolean = system.isCurrent

  // TODO: improve
  override def byPriority(system: OperatingSystem): ImplementationPriority =
    if (system.isCurrent) Allowed
    else NotAllowed
}

trait OperatingSystemComponentImpl extends OperatingSystemComponent {

  override val operatingSystem = resolve
}
