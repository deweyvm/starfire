package com.deweyvm.dogue.server

import com.deweyvm.gleany.logging.Logger
import com.deweyvm.dogue.common.Implicits._
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.dogue.server.db.DbConnection

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
    new DbConnection
    new Server().start()
  }
}
