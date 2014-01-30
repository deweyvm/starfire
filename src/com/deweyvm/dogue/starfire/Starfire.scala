package com.deweyvm.dogue.starfire

import java.net.{SocketTimeoutException, ServerSocket}
import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task
import scala.collection.mutable.ArrayBuffer


class Starfire(port:Int) {

  var running = true
  var readerId = 0

  var readers = ArrayBuffer[StarReader]()
  def execute() {
    Log.info("Starting server")
    val server = new ServerSocket(port)
    Log.info("Server started successfully")
    while(running && !server.isClosed) {
      Log.info("Awaiting connections")
      val connection = server.accept()
      val (running, stopped) = readers partition { _.isRunning }
      readers = running
      stopped foreach { _.kill() }
      Log.info("Spawning reader")
      val reader = new StarReader(connection, this, readerId)
      readerId += 1
      reader.start()
      readers += reader

    }
    Log.info("Shutting down")
  }

  def broadcast(string:String) {
    readers foreach { r =>
      r.
    }
  }

}

