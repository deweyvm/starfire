package com.deweyvm.dogue.starfire

import com.deweyvm.gleany.Debug
import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.Functions._
import java.net.Socket
import com.deweyvm.dogue.common.data.Encoding
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task

class StarWorker(string:String, socket:Socket) extends Task {
  override def execute() {
    (doCommand _ ∘ convert)(string)
  }

  private def doCommand(string:String) {
    val parts = string.esplit(' ')
    val command = parts(0)
    val rest = parts.drop(0).mkString(" ")
    if (command == "/quit") {
      Log.info("don't know how to quit :(")
    } else if (command == "/say") {
      Log.info("saying \"%s\" to the server" format rest)
      socket.transmit(rest)
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
