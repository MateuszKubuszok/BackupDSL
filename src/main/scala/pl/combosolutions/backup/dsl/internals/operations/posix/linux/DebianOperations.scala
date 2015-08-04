package pl.combosolutions.backup.dsl.internals.operations.posix.linux

import pl.combosolutions.backup.dsl.internals.operations.PlatformSpecific

object DebianOperations
  extends PlatformSpecific
  with LinuxCommonOperations
  with AptOperations
