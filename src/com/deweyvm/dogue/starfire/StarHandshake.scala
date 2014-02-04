package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.protocol.{DogueOps, Invalid, Command}
import com.deweyvm.dogue.common.logging.Log
import com.badlogic.gdx.Gdx
import com.deweyvm.dogue.common.data.Crypto
import com.deweyvm.dogue.starfire.db.DbConnection
import com.deweyvm.dogue.common.procgen.Name

//trait StarHandshakeState
//
//object StarHandshake {
//  case object Greet extends StarHandshakeState
//  case object WaitReply extends StarHandshakeState
//}
class HandshakeTimeout extends Exception

object StarHandshake {
  type SuccessCallback = String => Unit
  type FailureCallback = DogueSocket => Unit
  type Spawner = Task => Unit
  def begin(serverName:String, socket:DogueSocket, success:SuccessCallback, failure:FailureCallback) {
    val spawner = (a:Task) => {
      a.start()
      Log.info("Spawning in thread " + Thread.currentThread().getId)
    }
    spawner(new Greeting(serverName, socket, spawner, success, failure))
  }

  class Greeting(serverName:String, socket:DogueSocket, spawner:Spawner, success:SuccessCallback, failure:FailureCallback) extends Task {
    override def doWork() {
      Log.info("Greeting client")
      socket.transmit(new Command(DogueOps.Greet, serverName, "&unknown&", "identify"))
      kill()
      spawner(new Identify(serverName, socket, success, failure))
    }

    override def exception(t:Throwable) {
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
      socket.transmit(new Command(DogueOps.Greet, serverName, newName, "Handshake complete."))
      success(newName)
      kill()
    }

    private def handshakeFail(reason:String, socket:DogueSocket) {
      Log.warn("Handshake failure: " + reason)
      failure(socket)
      kill()
    }

    override def exception(t:Throwable) {
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

              success(src)
              kill()
            case DogueOps.Identify =>
              Log.info("Attempting to authenticate user")
              val username = args(0)
              val password = args(1)
              val dbd = new DbConnection().getPassword(username)
              dbd match {
                case Some((salt, hash)) =>
                  socket.transmit(new Command(DogueOps.Greet, serverName, username, "Looking up username..."))
                  if (Crypto.comparePassword(password, salt, hash)) {

                    socket.transmit(new Command(DogueOps.Greet, serverName, username, "Now identified as %s." format username))
                    socket.transmit(new Command(DogueOps.Greet, serverName, username, "Welcome!"))
                    success(username)
                    kill()
                  } else {
                    socket.transmit(new Command(DogueOps.Greet, serverName, username, "Incorrect password."))

                    identFail("Password for user \"%s\" was incorrect" format username, username)
                  }
                case None =>
                  socket.transmit(new Command(DogueOps.Greet, serverName, username, "User not found or database connection could not be established."))

                  identFail("User \"%s\" does not exist" format username, username)
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

  }


}

