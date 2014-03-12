package com.deweyvm.dogue.starfire.entities


class User(name:String, var isRegistered:Boolean) {
  def getSigilName:String = {
    val sigil = if (isRegistered) "☼" else "-"
    sigil + name
  }

  def getPlainName:String = name

}
