package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.protocol.{DogueOps, Command}
import com.deweyvm.dogue.common.data.Crypto
import com.deweyvm.dogue.starfire.db.StarDb

class StarWorker(cmd:Command, connection:StarConnection, socket:DogueSocket) extends Task {
  override def doWork() {
    doCommand(cmd)
    kill()
  }

  override def cleanup() {
    Log.all("Worker is done")
  }

  private def doCommand(command:Command) {
    import DogueOps._
    command.op match {
      case Close =>
        Log.info("Close requested by " + command.source)
        connection.close()
      case Say =>
        connection.broadcast(command.source, command.args(0))
      case Ping =>
        connection.pong()
        socket.transmit(Command(DogueOps.Pong, connection.serverName, command.source, Vector()))

      case Register =>
        val newNick = command.source
        if (!connection.nickInUse(newNick)) {
          val (password, salt, hash) = Crypto.generatePassword
          Log.info(hash)
          Log.info(salt)
          StarDb.createUser(newNick, salt, hash) match {
            case Right(_) =>
              socket.transmit(Command(DogueOps.Assign, connection.serverName, command.source, Vector(newNick, password)))
              socket.transmit(new Command(DogueOps.Greet, connection.serverName, command.source, "You have registered the name %s. Hope you like it!" format command.source))
            case Left(_) =>
              Log.warn("here")
              socket.transmit(new Command(DogueOps.Greet, connection.serverName, command.source, "Unable to register: Database error"))
          }

        } else {
          socket.transmit(Command(DogueOps.Greet, connection.serverName, command.source, Vector("That name is in use!")))
        }
      case _ =>
        Log.warn("Command \"%s\" unhandled in server." format command)
    }
  }

}
