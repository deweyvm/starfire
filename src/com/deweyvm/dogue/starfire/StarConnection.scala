package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.{ThreadManager, Task}
import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.protocol.{Invalid, Command, DogueMessage}


class StarConnection(val clientName:String, socket:DogueSocket, parent:Starfire, id:Int) extends Task {
  def serverName = parent.name
  private val ponger = ThreadManager.spawn(new StarPonger(this))
  socket.setTimeout(500)

  override def killAux() {
    ponger.kill()
  }

  override def cleanup() {
    Log.info("Connection to \"%s\" closed" format clientName)
    ponger.kill()
  }

  def write(s:DogueMessage) {
    socket.transmit(s)
  }

  override def doWork() {
    val commands = socket.receiveCommands()
    commands foreach { new StarWorker(_, this, socket).start() }
    Thread.sleep(500)
  }

  def pong() {
    ponger.pong()
  }

  def broadcast(from:String, string:String) {
    parent.broadcast(from, string)
  }

  def nickInUse(name:String):Boolean = {
    parent.nickInUse(name)
  }

  def close() {
    kill()
  }


}


