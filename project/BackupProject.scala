import sbt._

object BackupProject extends Build with Settings with Dependencies {

  lazy val common = project.from("common").
    configureCommon

  lazy val psm = project.from("psm").
    configureCommon.
    configurePlatform.
    configureFunctional.
    configureUnit.
    dependsOnProjects(common)

  lazy val dsl = project.from("dsl").
    configureCommon.
    configurePlatform.
    configureFunctional.
    configureUnit.
    dependsOnProjects(common, psm)
}
