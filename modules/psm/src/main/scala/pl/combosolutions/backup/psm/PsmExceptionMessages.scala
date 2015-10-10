package pl.combosolutions.backup.psm

object PsmExceptionMessages {

  val BadClassURL = "Invalid class URL"
  val NoElevationAvailable = "No elevation found"
  val NoFileSystemAvailable = "No file system found"
  val NoOperatingSystemAvailable = "No known operating system found"
  val NoRepositoriesAvailable = "No repositories found"
  val DirectCommand = "Command cannot be directly elevated"
  val RemoteFailure = "Failed to initiate remote executor"
  val RemoteGeneric = "Remote elevator cannot be converted into GenericProgram"
  val RemoteKilling = "Remote elevator isn't supposed to die before program finishes"
  val UnknownFileType = "Unexpected `file` answer"
}
