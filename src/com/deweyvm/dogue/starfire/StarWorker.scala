package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.Functions._
import java.net.Socket
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.protocol.{DogueOp, Command}
import com.deweyvm.dogue.common.parsing.CommandParser

class StarWorker(cmd:Command, connection:StarConnection, socket:DogueSocket) extends Task {
  override def doWork() {
    doCommand(cmd)
    kill()
  }

  override def cleanup() {
    Log.all("Worker is done")
  }

  private def doCommand(command:Command) {
    import DogueOp._
    command.op match {
      case Quit =>
        Log.info("Close requested by " + command.source)


        connection.close()
      case Say =>
        connection.broadcast(command.source, command.args.mkString(" "))//fixme issue #86
      case Ping =>
        connection.pong()
        socket.transmit(Command(DogueOp.Pong, connection.getName, command.source, Vector()))
      case _ =>
        Log.warn("Command \"%s\" unhandled in server." format command)
    }
  }
}
