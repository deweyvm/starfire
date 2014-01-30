package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.threading.Task
import com.deweyvm.dogue.common.logging.Log

class StarPong(reader:StarReader) extends Task {
  private var lastPong = System.currentTimeMillis()
  private val maxPingTimeMillis = 120*1000

  override def killAux() {
    reader.kill()
  }

  override def doWork() {
    Thread.sleep(250)
    if (System.currentTimeMillis - lastPong > maxPingTimeMillis) {
      kill()
    }
  }

  override def cleanup() {
    Log.info("Ponger dying")
  }

  def pong() {
    lastPong = System.currentTimeMillis
  }
}
