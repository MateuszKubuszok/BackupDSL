import sbt._

object BackupProject extends Build with Settings with Dependencies {

  lazy val common = project.from("common").
    settings(commonSettings:_*)

  lazy val psm = project.from("psm").
    configurePlatform.
    configureFunctional.
    configureUnit.
    settings(commonSettings:_*).
    dependsOnProjects(common)

  lazy val dsl = project.from("dsl").
    configurePlatform.
    configureFunctional.
    configureUnit.
    settings(commonSettings:_*).
    dependsOnProjects(common, psm)
}
