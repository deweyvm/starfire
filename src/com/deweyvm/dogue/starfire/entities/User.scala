package com.deweyvm.dogue.starfire.entities

import com.deweyvm.dogue.starfire.StarConnection
import com.deweyvm.dogue.common.protocol.{DogueOps, Command}
import com.deweyvm.dogue.common.data.Code
import com.deweyvm.dogue.common.logging.Log

class User(name:String, isRegistered:Boolean) {
  def getSigilName:String = {
    val sigil = if (isRegistered) "☼" else "-"
    sigil + name
  }

  def getPlainName:String = name

}
