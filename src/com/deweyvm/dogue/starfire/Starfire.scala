package com.deweyvm.dogue.starfire

import java.net.{Socket, SocketTimeoutException, ServerSocket}
import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task
import scala.collection.mutable.ArrayBuffer
import com.deweyvm.dogue.common.io.DogueServer
import com.deweyvm.dogue.common.protocol.Command
import com.deweyvm.dogue.starfire.db.DbConnection


class Starfire(val name:String, port:Int) {

  var running = true
  var readerId = 0

  var readers = ArrayBuffer[StarConnection]()
  def execute() {
    new DbConnection
    Log.info("Starting server")
    val server = new DogueServer("starfire", port)
    Log.info("Server started successfully")
    while(running) {
      Log.info("Awaiting connections on port " + port)
      val socket = server.accept()
      val (running, stopped) = readers partition { _.isRunning }
      readers = running
      stopped foreach { _.kill() }
      Log.info("Spawning reader")
      val connection = new StarConnection(socket, this, readerId)
      readerId += 1
      connection.start()
      readers += connection
    }
    Log.info("Shutting down")
  }

  def broadcast(string:String) {
    readers foreach { r =>
      r.write(Command("say", "SERVER", "fixme", Vector(string)))
    }
  }

}

