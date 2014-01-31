package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.Functions._
import java.net.Socket
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.protocol.Command
import com.deweyvm.dogue.common.parsing.CommandParser

object StarWorker {
  val parser = new CommandParser

}
class StarWorker(cmd:Command, connection:StarConnection, socket:DogueSocket) extends Task {
  import StarWorker._
  override def doWork() {
    doCommand(cmd)
    kill()
  }

  override def cleanup() {
    Log.verbose("Worker is done")
  }

  private def doCommand(command:Command) {
    if (command.op == "quit") {
      Log.info("don't know how to quit :(")
    } else if (command.op == "say") {
      connection.broadcast(command.args.mkString)
      socket.transmit(Command("say", connection.getName, "SERVER", Vector()))
    } else if (command.op == "ping") {
      connection.pong()
      socket.transmit(Command("pong", connection.getName, "SERVER", Vector()))
    }
  }
}
