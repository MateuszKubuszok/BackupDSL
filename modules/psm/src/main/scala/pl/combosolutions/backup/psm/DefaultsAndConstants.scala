package pl.combosolutions.backup.psm

import java.nio.file.CopyOption
import java.nio.file.StandardCopyOption._

import org.apache.commons.lang3.SystemUtils

object DefaultsAndConstants {

  val JavaHomeProperty = "java.home"
  val ClassPathProperty = "java.class.path"
  val RMICodebaseProperty = "java.rmi.server.codebase"
  val RMIDisableHttpProperty = "java.rmi.server.disableHttp"

  val BackupDirName = "backup_dsl"
  val BackupDirPath = java.nio.file.Paths.get(SystemUtils.getUserHome.getPath, BackupDirName)
  val CopyOptions = Array[CopyOption](REPLACE_EXISTING, COPY_ATTRIBUTES)

  val ProgramThreadPoolSize = 10
  val TaskThreadPoolSize = 10
}
