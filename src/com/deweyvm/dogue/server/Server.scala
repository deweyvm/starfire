package com.deweyvm.dogue.server

import java.net.{SocketTimeoutException, ServerSocket}
import com.deweyvm.gleany.net.Task
import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.logging.Log


class Server extends Task {
  val port = 4815
  var running = true
  //this doesnt scale to more than 1 concurrent connection
  var currentReader:Option[Reader] = None
  override def execute() {
    Log.info("Starting server")
    val server = new ServerSocket(port)
    server.setSoTimeout(1000)
    Log.info("Server started successfully")
    while(running && !server.isClosed) {
      try {
        val connection = server.accept()
        currentReader foreach {
          Log.info("Killing old reader")
          _.kill()
        }
        Log.info("Spawning reader")
        val reader = new Reader(connection, this)
        reader.start()
        currentReader = reader.some
      } catch {
        case ste:SocketTimeoutException =>
          Thread.sleep(100)
      }
    }
    Log.info("Shutting down")
  }

}

