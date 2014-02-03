package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.protocol.{DogueOps, Invalid, Command}
import com.deweyvm.dogue.common.logging.Log
import com.badlogic.gdx.Gdx

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
    Log.all("Beginning handshake")
  }

  override def doWork() {
    state match {
      case Greet =>
        socket.transmit(new Command(DogueOps.Greet, serverName, "&unknown&", "identify"))
        state = WaitReply
      case WaitReply =>
        Log.info("Waiting for reply")
        val commands = socket.receiveCommands()
        commands foreach {
          case cmd@Command(op, src, dst, args) =>
            val clientName = src
            op match {
              case DogueOps.Greet =>
                Log.all("Received greeting from " + clientName)
                socket.transmit(new Command(DogueOps.Greet, serverName, clientName, "Welcome!"))
                kill()
                acceptAction(clientName)
              case DogueOps.Quit =>
                Log.info("Handshake interrupted: client closed connection")
                kill()
              case _ =>
                Log.warn("Command \"%s\" ignored during handshake." format cmd.toString)
            }
          case inv@Invalid(_,_) =>
            inv.warn()
        }
    }
    Thread.sleep(500)
    iterations -= 1
    if (iterations <= 0) {
      throw new RuntimeException("Handshake timeout")
    }
  }

  override def cleanup() {
    Log.all("Handshake dying")
  }

}
