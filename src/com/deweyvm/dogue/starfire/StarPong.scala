package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.logging.Log

class StarPong(reader:StarReader) extends Task {
  private var running = true
  private var lastPong = System.currentTimeMillis()
  private val maxPingTimeMillis = 30*1000
  override def execute() {
    while(running) {
      Thread.sleep(250)
      if (System.currentTimeMillis - lastPong > maxPingTimeMillis) {
        reader.kill()
        running = false
      }
    }
    Log.info("Ponger dying")
  }

  def pong() {
    lastPong = System.currentTimeMillis
  }
}
