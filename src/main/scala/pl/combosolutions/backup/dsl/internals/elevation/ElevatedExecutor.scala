package pl.combosolutions.backup.dsl.internals.elevation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object ElevatedExecutor extends App {
  def arg2Int(arg: String) = Try(Integer.valueOf(arg)).toOption

  def ports = args.indices.map(i => arg2Int(args(i))).flatten

  def listenToPort(port: Integer) = ElevationServer(port).listen

  def socketsFutures = (ports map listenToPort)

  Future sequence socketsFutures
}
