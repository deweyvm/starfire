package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.logging.{LogLevel, Log}




object Main {
  def main(args:Array[String]) {
    val parser = new scopt.OptionParser[StarfireOptions]("starfire") {
      head("starfire", "0.X.X")

      opt[String]("log") action { (x, c) =>
        c.copy(logDir = x)
      } text "directory to place logs"

      opt[String]("log-level") action { (v, c) =>
        c.copy(logLevel = v)
      } text ("log level = {%s}" format Log.levels.map{_.toString.toLowerCase}.mkString(","))

      opt[Int]("port") action { (x, c) =>
        c.copy(port = x)
      } text "port to connect to"

      checkConfig { c =>
        if (c.port == 0) failure("port cannot be 0") else success
      }
    }
    parser.parse(args, StarfireOptions()) map { c =>
      val logLevel = LogLevel.fromString(c.logLevel)
      Log.initLog(c.logDir, logLevel)
      new Starfire("SERVER(flare)", c.port)
      ()
    } getOrElse {
      System.err.print(parser.usage + "\n")
    }

  }
}

