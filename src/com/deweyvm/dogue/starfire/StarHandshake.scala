package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.protocol.{DogueOps, Command}
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.data.Crypto
import com.deweyvm.dogue.starfire.db.StarDb
import com.deweyvm.dogue.common.procgen.Name
import com.deweyvm.gleany.data.Time
import com.deweyvm.dogue.common.Implicits
import Implicits._
//trait StarHandshakeState
//
//object StarHandshake {
//  case object Greet extends StarHandshakeState
//  case object WaitReply extends StarHandshakeState
//}
class HandshakeTimeout extends Exception

object StarHandshake {
  type SuccessCallback = (String,Boolean) => Unit
  type FailureCallback = DogueSocket => Unit
  def begin(serverName:String, socket:DogueSocket, success:SuccessCallback, failure:FailureCallback) {
    new Greeting(serverName, socket, success, failure).start().ignore()
  }

  class Greeting(serverName:String, socket:DogueSocket, success:SuccessCallback, failure:FailureCallback) extends Task {
    override def doWork() {
      Log.info("Greeting client")
      socket.transmit(new Command(DogueOps.Greet, serverName, Name.unknown, "identify"))
      kill()
      new Identify(serverName, socket, success, failure).start().ignore()
    }

    override def exception(t:Exception) {
      failure(socket)
    }
  }


  class Identify(serverName:String, socket:DogueSocket, success:SuccessCallback, failure:FailureCallback) extends Task {
    private var iters = 10

    private def identFail(reason:String, clientName:String) {
      Log.info("Identification failed: " + reason)
      val newName = new Name().get
      socket.transmit(new Command(DogueOps.Greet, serverName, clientName, reason))
      socket.transmit(new Command(DogueOps.Reassign, serverName, newName, reason))
      socket.transmit(new Command(DogueOps.Greet, serverName, newName, "Naming you %s instead" format newName))
      socket.transmit(new Command(DogueOps.Greet, serverName, newName, "Welcome!"))
      success(newName, false)
      kill()
    }

    private def handshakeFail(reason:String, socket:DogueSocket) {
      Log.warn("Handshake failure: " + reason)
      failure(socket)
      kill()
    }

    override def exception(t:Exception) {
      failure(socket)
    }

    override def doWork() {
      socket.receiveCommands() foreach {
        case cmd@Command(op, src, dst, args) =>
          op match {
            case DogueOps.Greet =>
              Log.info("User not registered. Sending greeting...")
              socket.transmit(new Command(DogueOps.Greet, serverName, src, "Welcome!"))
              socket.transmit(new Command(DogueOps.Greet, serverName, src, "You are called " + src))

              success(src, false)
              kill()
            case DogueOps.Identify =>
              Log.info("Attempting to authenticate user")
              if (args.length < 2) {
                identFail("Blank password and/or username given", Name.unknown)
                return
              }
              val username = args(0)
              val password = args(1)
              val dbd = StarDb.getPassword(username)
              dbd match {
                case Right((salt, hash, lastSeen)) =>
                  socket.transmit(new Command(DogueOps.Greet, serverName, username, "Looking up username..."))
                  if (Crypto.comparePassword(password, salt, hash)) {
                    val date = Time.epochToDate(lastSeen.toInt)
                    socket.transmit(new Command(DogueOps.Greet, serverName, username, "Now identified as %s." format username))
                    socket.transmit(new Command(DogueOps.Greet, serverName, username, "Last login on %s." format date))
                    socket.transmit(new Command(DogueOps.Greet, serverName, username, "Welcome!"))
                    see(username)

                    success(username, true)
                    kill()
                  } else {
                    identFail("Password for user \"%s\" was incorrect" format username, username)
                  }
                case Left(_) =>
                  identFail("User not found or database connection could not be established.", username)
              }

            case DogueOps.Close =>
              handshakeFail("Client closed connection", socket)
          }

      }
      Thread.sleep(500)
      iters -= 1
      if (iters <= 0) {
        handshakeFail("Timeout", socket)
      }
    }


    def see(name:String) = {

      StarDb.see(name) match {
        case Right(_) =>
          Log.info("Updated last seen date of user " + name)
        case Left(_) =>
          Log.warn("Failed to update last seen date of user " + name)
      }

    }

  }


}

