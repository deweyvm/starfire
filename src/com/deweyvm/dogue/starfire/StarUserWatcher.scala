package com.deweyvm.dogue.starfire

import com.deweyvm.dogue.common.threading.{Lock, Task}

class StarUserWatcher extends Task {
  override def doWork() {
    Thread.sleep(500)
  }
}
