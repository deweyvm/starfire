package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.protocol.{DogueOp, Invalid, Command}
import com.deweyvm.dogue.common.logging.Log

trait StarHandshakeState

object StarHandshake {
  case object Greet extends StarHandshakeState
  case object WaitReply extends StarHandshakeState
}

class HandshakeTimeout extends Exception

class StarHandshake(serverName:String, socket:DogueSocket, acceptAction:(String) => Unit) extends Task {
  import StarHandshake._
  private var state:StarHandshakeState = Greet
  private var iterations = 10

  override def init() {
    Log.info("Beginning handshake")
  }

  override def doWork() {
    state match {
      case Greet =>
        socket.transmit(new Command(DogueOp.Greet, serverName, "&unknown&", "identify"))
        state = WaitReply
      case WaitReply =>
        val commands = socket.receiveCommands()
        commands foreach {
          case cmd@Command(op, src, dst, args) =>
            val clientName = src
            if (op == DogueOp.Greet) {
              Log.info("Received greeting from " + clientName)
              socket.transmit(new Command(DogueOp.Greet, serverName, clientName, "Welcome!"))
              kill()
              acceptAction(clientName)
            } else {
              Log.warn("Command \"%s\" ignored during handshake." format cmd.toString)
            }
          case Invalid(msg) =>
            Log.warn("Invalid command " + msg)
        }
    }
    Thread.sleep(500)
    iterations -= 1
    if (iterations <= 0) {
      throw new RuntimeException("Handshake timeout")
    }
  }

  override def cleanup() {
    Log.verbose("Handshake dying")
  }

}
