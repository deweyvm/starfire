package com.deweyvm.dogue.starfire

import java.net.Socket
import scala.collection.mutable.ArrayBuffer
import java.io.InputStream
import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.gleany.Debug
import com.deweyvm.dogue.common.data.Encoding
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.Task


class StarReader(socket:Socket, parent:Starfire) extends Task {
  private var running = true
  private val inBuffer = ArrayBuffer[String]()
  private var current = ""

  def isRunning:Boolean = running

  def kill() {
    running = false
  }

  override def execute() {
    while(running && !socket.isClosed) {
      Log.info("Reading data")
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
          new StarWorker(s, socket/*fixme should probably be another socket?*/).start()
        }
        inBuffer.clear()
      }
      if (!read.isEmpty) {
        Thread.sleep(350)
      }
    }
    running = false
    Log.info("Killed")
  }


}


