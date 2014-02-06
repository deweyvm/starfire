package com.deweyvm.dogue.starfire.entities


class User(name:String, val isRegistered:Boolean) {
  def getSigilName:String = {
    val sigil = if (isRegistered) "☼" else "-"
    sigil + name
  }

  def getPlainName:String = name

}
