package com.deweyvm.dogue.starfire

import java.net.{Socket, SocketTimeoutException, ServerSocket}
import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task
import scala.collection.mutable.ArrayBuffer
import com.deweyvm.dogue.common.io.{DogueSocket, DogueServer}
import com.deweyvm.dogue.common.protocol.{DogueOps, Command}
import com.deweyvm.dogue.starfire.db.StarDb
import com.deweyvm.dogue.common.procgen.Name
import com.deweyvm.dogue.starfire.entities.User
import java.sql.Connection
import com.deweyvm.gleany.data.Time


class Starfire(val name:String, port:Int) {
  val minCrashTime = 5
  var running = true
  var crashes = Vector[Int]()

  while(running) {
    try {
      new StarfireInstance(name,port).execute()
    } catch {
      case t:Exception =>
        Log.error(Log.formatStackTrace(t))
        Log.error("Restarting starfire")
        val time = Time.epochTime
        if (crashes.length > 0) {
          val last = crashes(crashes.length - 1)
          if (time - last < minCrashTime) {
            Log.error("Too many crashes in too short a span: giving up")
            running = false
          }
        }
        crashes = crashes ++ Vector(time)
    }
  }
}

class StarfireInstance(val name:String, port:Int) {
  private var running = true
  private var connections = ArrayBuffer[StarConnection]()

  def execute() {
    Log.info("Starting server")
    val server = new DogueServer(name, port)
    Log.info("Server started successfully")
    while(running) {
      listen(server)
    }
    Log.all("Shutting down")
  }

  def listen(server:DogueServer) {
    Log.info("Awaiting connections on port " + port)
    val socket = server.accept()
    Log.info("Accepted connection from " + socket.ip)
    def onComplete(clientName:String, isRegistered:Boolean) {
      val (running, stopped) = connections partition { _.isRunning }
      connections = running
      stopped foreach { _.kill() }
      Log.info("Spawning reader " + isRegistered)
      val user = new User(clientName, isRegistered)
      val connection = new StarConnection(user, socket, this)
      connection.start()
      connections += connection
    }

    def onFailure(socket:DogueSocket) {
      socket.transmit(new Command(DogueOps.Close, name, Name.unknown))
      socket.close()
    }
    StarHandshake.begin(name, socket, onComplete, onFailure)

  }

  def broadcast(from:String, sigilName:String, s:String) {
    connections foreach { c =>
      c.write(Command(DogueOps.Say, from, c.user.getPlainName, Vector(sigilName, s)))
    }
  }

  def nickInUse(name:String, connection:StarConnection):Boolean = {
    connections exists {c => c == connection && c.user.getPlainName == name}
  }

}

