package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.logging.Log

object Main {
  def main(args:Array[String]) {
    if (args.contains("--pass")) {
      System.exit(0)
    }
    val logIndex = args.indexOf("--log")
    val logDir =
      if (logIndex != -1) {
        args(logIndex + 1)
      } else {
        "."
      }
    Log.setDirectory(logDir)
    //new DbConnection
    new Starfire().start()
  }
}

