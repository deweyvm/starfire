package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.common.threading.{ThreadManager, Task}
import com.deweyvm.dogue.common.io.DogueSocket
import com.deweyvm.dogue.common.protocol.{Invalid, Command, DogueMessage}
import com.deweyvm.dogue.starfire.entities.User


class StarConnection(val user:User, socket:DogueSocket, parent:StarfireInstance, time:String) extends Task {
  def serverName = parent.name
  private val ponger = ThreadManager.spawn(new StarPonger(this))
  socket.setTimeout(500)

  override def killAux() {
    ponger.kill()
  }

  override def cleanup() {
    Log.info("Connection to \"%s\" established at %s closed." format (user.getPlainName, time))
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

  def broadcast(from:String, sigilName:String, string:String) {
    parent.broadcast(from, sigilName, string)
  }

  def nickInUse(name:String):Boolean = {
    parent.nickInUse(name, this)
  }

  def close() {
    kill()
  }


}


