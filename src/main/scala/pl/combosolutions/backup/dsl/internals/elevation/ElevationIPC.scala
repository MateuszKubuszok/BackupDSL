package pl.combosolutions.backup.dsl.internals.elevation

import java.io._
import java.net.{InetAddress, ServerSocket, Socket}

import pl.combosolutions.backup.dsl.internals.operations.{GenericProgram, Result}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

// based on:
//   http://www.scala-sbt.org/0.12.2/sxr/IPC.scala.html
//   https://gist.github.com/ramn/5566596

object ElevationIPC {

  private val portMin     = 1025
  private val portMax     = 65536
  private val loopback    = InetAddress.getByName(null) // loopback
  private val maxAttempts = 10

  def ipcClient[T](port: Int): Socket = new Socket(loopback, port)

  def ipcServer: ServerSocket = {
    val random = new java.util.Random

    def nextPort = random.nextInt(portMax - portMin + 1) + portMin

    @tailrec
    def createServer(attempts: Int): ServerSocket =
      if(attempts > 0)
        Try (new ServerSocket(nextPort, 1, loopback)) match {
          case Success(socket) => socket
          case Failure(ex)     => createServer(attempts - 1)
        }
      else
        throw new IllegalStateException("Could not connect to socket: maximum attempts exceeded")

    createServer(maxAttempts)
  }
}

class ElevationIPCSocket(socket: Socket) {

  lazy val port = socket.getLocalPort

  private val in = new ObjectInputStream(socket.getInputStream)
  private val out = new ObjectOutputStream(socket.getOutputStream)

  def isListening = !socket.isClosed

  def close: Unit = {
    Try (out.writeObject(CloseConnection))
    Try (socket.close)
  }

  def error: Unit = Try (out.writeObject(ExecutionFailed))

  def send(program: GenericProgram): Unit = Try (out.writeObject(program))

  def send(result: Result[GenericProgram]): Unit = Try (out.writeObject(result))

  def receiveResult: Option[Result[GenericProgram]] = in.readObject match {
    case result: Result[_] => Some(result.asInstanceOf[Result[GenericProgram]])
    case CloseConnection   => close
                              None
    case ExecutionFailed      => None
    case _                    => None
  }

  def receiveProgram: Option[GenericProgram] = in.readObject match {
    case program: GenericProgram => Some(program)
    case CloseConnection         => close
                                    None
    case ExecutionFailed         => None
    case _                       => None
  }
}

case object CloseConnection
case object ExecutionFailed
