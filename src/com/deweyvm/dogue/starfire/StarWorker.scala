package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.Functions._
import java.net.Socket
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.io.DogueSocket

class StarWorker(string:String, reader:StarConnection, socket:DogueSocket) extends Task {

  override def doWork() {
    (doCommand _ ∘ convert)(string)
    kill()
  }

  override def cleanup() {
    Log.verbose("Worker is done")
  }

  private def doCommand(string:String) {
    val parts = string.esplit(' ')
    val command = parts(0)
    val rest = parts.drop(0).mkString(" ")
    if (command == "/quit") {
      Log.info("don't know how to quit :(")
    } else if (command == "/say") {
      Log.info("saying \"%s\" to all clients" format rest)
      socket.transmit(rest)
    } else if (command == "/ping") {
      reader.pong()
      socket.transmit("/pong")
    }
  }

  private def convert(string:String):String = {
    val commandPrefix =
      if (string.length > 0 && string(0) != '/') {
        "/say "
      } else {
        ""
      }
    commandPrefix + string
  }
}
