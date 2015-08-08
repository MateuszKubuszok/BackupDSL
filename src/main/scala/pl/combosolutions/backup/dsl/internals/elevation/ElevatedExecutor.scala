package pl.combosolutions.backup.dsl.internals.elevation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object ElevatedExecutor extends App {

  println(getClass)

  def arg2Int(arg: String) = Try(Integer.valueOf(arg)).toOption

  def ports(args: Array[String]) = args.indices.map(i => arg2Int(args(i))).flatten

  def listenToPort(port: Integer) = ElevationServer(port).listen

  def socketsFutures(args: Array[String]) = (ports(args) map listenToPort)

  Future sequence socketsFutures(args)
}
