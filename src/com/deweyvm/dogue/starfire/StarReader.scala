package com.deweyvm.dogue.starfire

import java.net.Socket
import scala.collection.mutable.ArrayBuffer
import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task


class StarReader(socket:Socket, parent:Starfire, id:Int) extends Task {
  private var running = true
  private val inBuffer = ArrayBuffer[String]()
  private var current = ""

  private val ponger = new StarPong(this)
  ponger.start()
  def isRunning:Boolean = running

  def kill() {
    running = false
    Log.info("Attempting to kill reader thread")
  }
  socket.setSoTimeout(500)
  override def execute() {
    while(running && !socket.isClosed) {
      val read = socket.receive()
      read foreach { next =>
        Log.info("Got data: " + next)
        val lines = next.esplit('\0')
        val last = lines(lines.length - 1)
        val first = lines.dropRight(1)
        for (s <- first) {
          current += s
          inBuffer += current
          current = ""
        }
        current = last

        for (s <- inBuffer) {
          new StarWorker(s, this, socket/*fixme should probably be a different socket?*/).start()
        }
        inBuffer.clear()
      }
    }
    Log.info("Reader closed")
  }

  def pong() {
    ponger.pong()
  }


}


