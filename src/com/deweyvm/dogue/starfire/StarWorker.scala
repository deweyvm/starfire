package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.Functions._
import java.net.Socket
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.protocol.{DogueOps, Command}
import com.deweyvm.dogue.common.parsing.CommandParser
import com.deweyvm.dogue.common.data.{Crypto, Encoding, GenUtils}
import java.security.{MessageDigest, SecureRandom}
import com.deweyvm.dogue.starfire.db.DbConnection
import com.deweyvm.dogue.common.procgen.Name

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
        connection.broadcast(command.source, command.args(0))//fixme issue #86
      case Ping =>
        connection.pong()
        socket.transmit(Command(DogueOps.Pong, connection.serverName, command.source, Vector()))

      case Nick =>
        val newNick = command.args(0)
        if (!connection.nickInUse(newNick)) {
          val (password, salt, hash) = Crypto.generatePassword
          Log.info(hash)
          Log.info(salt)
          new DbConnection().setPassword(newNick, salt, hash)
          socket.transmit(Command(DogueOps.Assign, connection.serverName, command.source, Vector(newNick, password)))
        }
      case _ =>
        Log.warn("Command \"%s\" unhandled in server." format command)
    }
  }

}
