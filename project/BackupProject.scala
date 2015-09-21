import sbt._
import sbt.Keys._

object BackupProject extends Build with Settings with Dependencies {

  lazy val common = (
    project in file("modules/common")
            settings (commonSettings:_*)
  )

  lazy val psm = (
    project in file("modules/psm")
            settings (commonSettings:_*)
            dependsOn (common % "test->test;compile->compile")
  )

  lazy val dsl = (
    project in file("modules/dsl")
            settings (commonSettings:_*)
            dependsOn (common % "test->test;compile->compile")
            dependsOn (psm % "test->test;compile->compile")
  )
}
