import sbt._
import sbt.Keys._

object BackupProject extends Build with Settings with Dependencies {

  val dependsOnCompileAndTest = "test->test;compile->compile"

  val commonDir = "modules"

  lazy val common = (
    project in file(s"$commonDir/common")
            settings (commonSettings:_*)
  )

  lazy val psm = (
    project in file(s"$commonDir/psm")
            configs (commonConfigs:_*)
            settings (commonSettings:_*)
            dependsOn (common % dependsOnCompileAndTest)
  )

  lazy val dsl = (
    project in file(s"$commonDir/dsl")
            configs (commonConfigs:_*)
            settings (commonSettings:_*)
            dependsOn (common % dependsOnCompileAndTest)
            dependsOn (psm % dependsOnCompileAndTest)
  )
}
