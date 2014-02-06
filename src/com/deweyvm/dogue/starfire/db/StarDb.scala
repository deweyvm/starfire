package com.deweyvm.dogue.starfire.db

import java.sql._
import com.deweyvm.dogue.common.logging.Log
import com.deweyvm.gleany.data.Time


object StarDb {
  val url = DbSettings.url
  val user = DbSettings.user
  val password = DbSettings.password

  private def warn[T](ex:Throwable):Left[String,T] = {
    val trace = Log.formatStackTrace(ex)
    Log.warn(trace)
    Left(Log.formatStackTrace(ex))
  }

  def createUser(name:String, salt:String, hash:String):Either[String,Unit] = {
    try {
      val connection = DriverManager.getConnection(url, user, password)
      val time = Time.epochTime
      val p = connection.prepareStatement(
        "INSERT INTO dogueusers(username, salt, hash, created, lastseen) VALUES(?, ?, ?, ?, ?) ;"
      )
      p.setString(1, name)
      p.setString(2, salt)
      p.setString(3, hash)
      p.setInt(4, time)
      p.setInt(5, time)
      p.executeUpdate()
      p.close()
      Right(())
    } catch {
      case ex:SQLException =>
        warn(ex)
    }
  }

  def getPassword(name:String):Either[String, (String,String,String)] = {
    try {
      val connection = DriverManager.getConnection(url, user, password)
      val p = connection.prepareStatement("SELECT salt, hash, lastseen FROM dogueusers where username=? ;")
      p.setString(1, name)
      val result = p.executeQuery()
      result.next()
      val salt = result.getString("salt")
      val hash = result.getString("hash")
      val lastSeen = result.getString("lastseen")
      p.close()
      Right((salt, hash, lastSeen))
    } catch {
      case ex:SQLException =>
        warn(ex)
    }
  }

  def see(name:String):Either[String,Unit] = {
    try {
      val connection = DriverManager.getConnection(url, user, password)
      val p = connection.prepareStatement(
        "UPDATE dogueusers SET lastseen = ? WHERE username = ? ;"
      )
      p.setInt(1, Time.epochTime)
      p.setString(2, name)
      p.executeUpdate()
      p.close()
      Right(())
    } catch {
      case ex:SQLException =>
        warn(ex)

    }
  }

}
