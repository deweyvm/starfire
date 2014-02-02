package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.Functions._
import java.net.Socket
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.protocol.{DogueOp, Command}
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
    Log.all("Worker is done")
  }

  private def doCommand(command:Command) {
    if (command.op == DogueOp.Quit) {
      Log.info("don't know how to quit :(")
    } else if (command.op == DogueOp.Say) {
      connection.broadcast(command.source, command.args(0))//fixme issue #79
    } else if (command.op == DogueOp.Ping) {
      connection.pong()
      socket.transmit(Command(DogueOp.Pong, connection.getName, command.source, Vector()))
    }
  }
}
