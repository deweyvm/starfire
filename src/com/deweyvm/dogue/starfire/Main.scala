package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.logging.Log




object Main {
  def main(args:Array[String]) {
    val port = 4815

    val parser = new scopt.OptionParser[StarfireOptions]("starfire") {
      head("starfire", "testing.0")

      opt[String]("log") action { (x, c) =>
        c.copy(logDir = x)
      } text "directory to place logs"

    }
    parser.parse(args, StarfireOptions()) map { c =>
      Log.initLog(c.logDir, Log.Verbose)
      new Starfire(port).execute()
    } getOrElse {
      println(parser.usage)
      throw new RuntimeException("invalid args")
    }

  }
}

