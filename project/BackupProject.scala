import sbt._

object BackupProject extends Build with Settings with Dependencies {

  lazy val root = project.root
    .setName("BackupDSL")
    .setDescription("Backup DSL")
    .setInitialCommand("_")
    .configureRoot
    .aggregate(common, psm, tasks, dsl)

  lazy val common = project.from("common")
    .setName("backup-common")
    .setDescription("Backup DSL: Common")
    .setInitialCommand("_")
    .configureModule

  lazy val psm = project.from("psm")
    .setName("backup-psm")
    .setDescription("Backup DSL: Platform Specific Module")
    .setInitialCommand("psm._")
    .configureModule
    .configurePlatformTests
    .configureFunctionalTests
    .configureUnitTests
    .dependsOnProjects(common)

  lazy val tasks = project.from("tasks")
    .setName("backup-tasks")
    .setDescription("Backup DSL: Tasks")
    .setInitialCommand("tasks._")
    .configureModule
    .configureFunctionalTests
    .configureUnitTests
    .dependsOnProjects(common, psm)

  lazy val dsl = project.from("dsl")
    .setName("backup-dsl")
    .setDescription("Backup DSL: DSL")
    .setInitialCommand("dsl._")
    .configureModule
    .configureFunctionalTests
    .configureUnitTests
    .dependsOnProjects(common, tasks)
}
