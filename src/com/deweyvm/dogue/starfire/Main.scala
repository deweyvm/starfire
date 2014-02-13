package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.logging.Log




object Main {
  def main(args:Array[String]) {
    val parser = new scopt.OptionParser[StarfireOptions]("starfire") {
      head("starfire", "0.X.X")

      opt[String]("log") action { (x, c) =>
        c.copy(logDir = x)
      } text "directory to place logs"

      opt[Int]("port") action { (x, c) =>
        c.copy(port = x)
      } text "port to connect to"

      checkConfig { c =>
        if (c.port == 0) failure("port cannot be 0") else success
      }
    }
    parser.parse(args, StarfireOptions()) map { c =>
      Log.initLog(c.logDir, Log.Verbose)
      new Starfire("SERVER(flare)", c.port)
      ()
    } getOrElse {
      System.err.print(parser.usage + "\n")
    }

  }
}

